package edp.davinci.service.types;

import com.alibaba.fastjson.JSON;
import edp.davinci.core.common.Constants;
import edp.davinci.core.enums.types.DateTypeEnum;
import edp.davinci.core.model.SqlFilter;
import edp.davinci.dto.viewDto.TypeGroup;
import edp.davinci.model.mdm.Department;
import edp.davinci.service.ExternalService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author linda
 */
@Slf4j
@Component
public class TypeGroupService {

    @Autowired
    private ExternalService externalService;

    /**
     * impala 维度聚合运算转换
     *
     * @param typeGroups
     * @param keywordPrefix
     * @param keywordSuffix
     * @return
     */
    public List<TypeGroup> toTypeGroups(List<TypeGroup> typeGroups, List<String> filterStrs, String keywordPrefix, String keywordSuffix) {
        if (typeGroups != null && typeGroups.size() > 0) {
            typeGroups = typeGroups.stream().map(typeGroup -> {
                String visualType = typeGroup.getVisualType();
                if ("date".equals(visualType)) {
                    buildDateTypeGroup(typeGroup, keywordPrefix, keywordSuffix);
                }
                if ("department".equals(visualType)) {
                    buildDepartmentTypeGroup(typeGroup, filterStrs, keywordPrefix, keywordSuffix);
                }
                return typeGroup;
            }).collect(Collectors.toList());
        }

        return typeGroups;
    }

    /**
     * 封装日期类型维度
     *
     * @param typeGroup
     * @param keywordPrefix
     * @param keywordSuffix
     */
    private void buildDateTypeGroup(TypeGroup typeGroup, String keywordPrefix, String keywordSuffix) {
        String value = typeGroup.getValue();
        String column = typeGroup.getColumn();
        typeGroup.setColumnAlias(column + '_' + value);
        for (DateTypeEnum dateTypeEnum : DateTypeEnum.values()) {
            if (dateTypeEnum.name().equals(value)) {
                ST aggSt = new ST(dateTypeEnum.getAgg());
                aggSt.add("keywordPrefix", keywordPrefix);
                aggSt.add("column", column);
                aggSt.add("keywordSuffix", keywordSuffix);
                if (DateTypeEnum.YQ.name().equals(value)) {
                    //构造参数， 原有的被传入的替换
                    STGroup stg = new STGroupFile(Constants.TYPE_GROUP_TEMPLATE);
                    ST columeAggSt = stg.getInstanceOf("yqColumeSql");
                    columeAggSt.add("typeGroup", typeGroup);
                    typeGroup.setColumnAgg(columeAggSt.render());
                }
                typeGroup.setAgg(aggSt.render());
                break;
            }
        }
    }

    /**
     * 封装部门类型维度
     *
     * @param typeGroup
     * @param keywordPrefix
     * @param keywordSuffix
     */
    private void buildDepartmentTypeGroup(TypeGroup typeGroup, List<String> filterStrs, String keywordPrefix, String keywordSuffix) {
        String column = typeGroup.getColumn();
        List<Department> departments = externalService.queryDepartments();
        List<SqlFilter> filters = filterStrs.stream().map(v -> JSON.parseObject(v, SqlFilter.class)).collect(Collectors.toList()).stream().filter(v -> "department".equals(v.getVisualType()) && v.getName().equals(column)).collect(Collectors.toList());
        List<String> nextDepartMent = new ArrayList<>();
        if (filters == null || filters.size() == 0) {
            List<Department> rootDepartMents = departments.stream().filter(v -> v.getParentSerialNo() == null || v.getParentSerialNo().isEmpty()).collect(Collectors.toList());
            if (rootDepartMents.size() > 0) {
                nextDepartMent = departments.stream().filter(v -> v.getParentSerialNo() != null && v.getParentSerialNo().equals(rootDepartMents.get(0).getSerialNo())).map(v -> v.getDisplayName()).collect(Collectors.toList());
                nextDepartMent.add(0,rootDepartMents.get(0).getDisplayName() + "$");
            }
        } else {
            SqlFilter sqlFilter = filters.get(0);
            String[] values = sqlFilter.getValue().toString().split("\\|");
            if (values.length == 1) {
                List<Department> rootDepartMents = departments.stream().filter(v -> values[0].equals(v.getDisplayName())).collect(Collectors.toList());
                if (rootDepartMents.size() > 0) {
                    nextDepartMent = departments.stream().filter(v -> v.getParentSerialNo() != null && v.getParentSerialNo().equals(rootDepartMents.get(0).getSerialNo())).map(v -> v.getDisplayName()).collect(Collectors.toList());
                    nextDepartMent.add(0,rootDepartMents.get(0).getDisplayName() + "$");
                }
                if (nextDepartMent.size() > 0) {
                    filters = filters.stream().filter(v -> !v.getName().equals(column)).collect(Collectors.toList());
                }
            }
        }
        if (nextDepartMent.size() > 0) {
            String value = StringUtils.join(nextDepartMent, "|");
            SqlFilter sqlFilter = new SqlFilter();
            sqlFilter.setName(typeGroup.getColumn());
            sqlFilter.setOperator("regexp");
            sqlFilter.setValue(value);
            sqlFilter.setVisualType("department");
            sqlFilter.setSqlType("STRING");
            sqlFilter.setType("filter");
            filters.add(sqlFilter);
            filterStrs.add(JSON.toJSONString(sqlFilter));
        }
        if (filters.size() == 0) {
            return;
        }
        String agg = " regexp_extract(<keywordPrefix><column><keywordSuffix>,'(<value>)',0)";

        for (SqlFilter sqlFilter : filters) {
            String value = sqlFilter.getValue().toString();
            String name = sqlFilter.getName();
            typeGroup.setColumnAlias(name + "_D");
            ST aggSt = new ST(agg);
            aggSt.add("keywordPrefix", keywordPrefix);
            aggSt.add("column", name);
            aggSt.add("keywordSuffix", keywordSuffix);
            aggSt.add("value", value);
            typeGroup.setAgg(aggSt.render());
            break;
        }
    }


    /**
     * 去掉需要聚合的维度
     *
     * @param groups
     * @param typeGroups
     * @return
     */
    public List<String> groupsFilter(List<String> groups, List<TypeGroup> typeGroups) {
        if (groups != null && typeGroups != null && typeGroups.size() > 0) {
            groups = groups.stream().filter(group -> !typeGroups.stream().map(typeGroup -> typeGroup.getColumn()).collect(Collectors.toList()).contains(group)).collect(Collectors.toList());
        }
        return groups;
    }
}
