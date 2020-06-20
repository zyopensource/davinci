package edp.davinci.service.types;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import edp.davinci.core.common.Constants;
import edp.davinci.core.enums.types.DateTypeEnum;
import edp.davinci.core.enums.types.LevelTypeEnum;
import edp.davinci.core.model.SqlFilter;
import edp.davinci.dto.viewDto.Model;
import edp.davinci.dto.viewDto.TypeGroup;
import edp.davinci.model.mdm.CostCenter;
import edp.davinci.model.mdm.Department;
import edp.davinci.model.mdm.LevelData;
import edp.davinci.model.mdm.Subject;
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
     * @param groups
     * @param filterStrs
     * @param keywordPrefix
     * @param keywordSuffix
     * @return
     */
    public List<TypeGroup> toTypeGroups(List<String> groups, List<String> filterStrs, String keywordPrefix, String keywordSuffix, String model) {
        List<TypeGroup> typeGroups = new ArrayList<>();
        if (groups != null) {
            groups = groups.stream().distinct().collect(Collectors.toList());
            for (String group : groups) {
                if (isTypeGroup(group)) {
                    TypeGroup typeGroup = JSON.parseObject(group, TypeGroup.class);
                    typeGroups.add(typeGroup);
                } else {
                    JSONObject modelObj = JSON.parseObject(model);
                    Model modelVal = JSON.parseObject(modelObj.getString(group), Model.class);
                    TypeGroup typeGroup = new TypeGroup(group, modelVal.getVisualType(), "");
                    typeGroups.add(typeGroup);
                }
            }
        }
        if (typeGroups != null && typeGroups.size() > 0) {
            typeGroups = typeGroups.stream().map(typeGroup -> {
                String visualType = typeGroup.getVisualType();
                if ("date".equals(visualType)) {
                    buildDateTypeGroup(typeGroup, keywordPrefix, keywordSuffix);
                }
                if (LevelTypeEnum.Department.getName().equals(visualType)) {
                    buildDepartmentTypeGroup(typeGroup, filterStrs, keywordPrefix, keywordSuffix);
                }
                if (LevelTypeEnum.CostCenter.getName().equals(visualType)) {
                    buildCostCenterTypeGroup(typeGroup, filterStrs, keywordPrefix, keywordSuffix);
                }
                if (LevelTypeEnum.Subject.getName().equals(visualType)) {
                    buildSubjectTypeGroup(typeGroup, filterStrs, keywordPrefix, keywordSuffix);
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
                    STGroup typeGroupStg = new STGroupFile(Constants.TYPE_GROUP_TEMPLATE);
                    ST columeAggSt = typeGroupStg.getInstanceOf("yqColumeSql");
                    columeAggSt.add("typeGroup", typeGroup);
                    typeGroup.setColumnAgg(columeAggSt.render());
                }
                typeGroup.setAgg(aggSt.render());
                break;
            }
        }
    }


    private void buildTypeGroup(List<LevelData> levelDatas, TypeGroup typeGroup, List<String> filterStrs, String keywordPrefix, String keywordSuffix, String visualType) {
        String column = typeGroup.getColumn();
        //获取对应类型和对应维度内的过滤数据
        List<SqlFilter> filters = filterStrs.stream()
                .map(v -> JSON.parseObject(v, SqlFilter.class))
                .filter(v ->
                        visualType.equals(v.getVisualType())
                                && v.getName().equals(column)
                )
                .collect(Collectors.toList());
        //下一级节点的数据集合
        List<String> nextLevel = new ArrayList<>();
        //没有对应类型的过滤（默认按照最上级节点数据展示）
        if (filters == null || filters.size() == 0) {
            //获取根节点
            List<LevelData> rootLevels = levelDatas.stream().filter(v -> v.getParentId() == null || v.getParentId().isEmpty()).collect(Collectors.toList());
            //获取下一级节点数据
            if (rootLevels.size() > 0) {
                nextLevel = levelDatas.stream().filter(v -> v.getParentId() != null && v.getParentId().equals(rootLevels.get(0).getId())).map(v -> v.getLongName()).collect(Collectors.toList());
                //根节点也要加上
                nextLevel.add(0, rootLevels.get(0).getLongName() + "$");
            }
        } else if (filters.size() == 1) {
            SqlFilter sqlFilter = filters.get(0);
            String[] values = sqlFilter.getValue().toString().split("\\|");
            //过滤条件下只有一个节点（展示下一个下一个节点的数据）
            if (values.length == 1) {
                //获取当前节点
                List<LevelData> rootLevels = levelDatas.stream().filter(v -> values[0].equals(v.getLongName())).collect(Collectors.toList());
                if (rootLevels.size() > 0) {
                    //获取当前节点下一级节点
                    nextLevel = levelDatas.stream().filter(v -> v.getParentId() != null && v.getParentId().equals(rootLevels.get(0).getId())).map(v -> v.getLongName()).collect(Collectors.toList());
                    //当前节点也要加上
                    nextLevel.add(0, rootLevels.get(0).getLongName() + "$");
                }
            }
        }
        //如果filters过滤的值超过一个，直接跳过（两个层级过滤是不会展示下一级节点）
        if (nextLevel.size() > 0) {

            String value = StringUtils.join(nextLevel, "|");
            SqlFilter sqlFilter = new SqlFilter();
            sqlFilter.setName(column);
            sqlFilter.setOperator("regexp");
            sqlFilter.setValue(value);
            sqlFilter.setVisualType(visualType);
            sqlFilter.setSqlType("STRING");
            sqlFilter.setType("filter");
            //清除维度之前过滤的条件（因为有了新的维度过滤了nextLevel）
            filters = filters.stream().filter(v -> !v.getName().equals(column)).collect(Collectors.toList());
            filters.add(sqlFilter);
        }
        //没有过滤条件就不需要继续了
        if (filters.size() == 0) {
            return;
        }
        typeGroup.setColumnAlias(column + "_" + visualType);
        //获取过滤条件的所有值
        List<Object> values = filters.stream().map(v -> v.getValue()).collect(Collectors.toList());
        STGroup typeGroupStg = new STGroupFile(Constants.TYPE_GROUP_TEMPLATE);
        ST aggSt = typeGroupStg.getInstanceOf("levelAgg");
        aggSt.add("keywordPrefix", keywordPrefix);
        aggSt.add("column", column);
        aggSt.add("keywordSuffix", keywordSuffix);
        aggSt.add("values", values);
        typeGroup.setValue(StringUtils.join(values, "|"));
        typeGroup.setAgg(aggSt.render());
    }

    /**
     * 封装部门类型维度
     *
     * @param typeGroup
     * @param keywordPrefix
     * @param keywordSuffix
     */
    private void buildCostCenterTypeGroup(TypeGroup typeGroup, List<String> filterStrs, String keywordPrefix, String keywordSuffix) {
        List<CostCenter> costCenters = externalService.queryCostCenters();
        List<LevelData> levelDatas = costCenters.stream().map(v -> {
            LevelData levelData = new LevelData(v.getDepartmentId(), v.getDepartmentName(), v.getDepartmentLongName(), v.getSuperiorDepartmentId());
            return levelData;
        }).collect(Collectors.toList());
        buildTypeGroup(levelDatas, typeGroup, filterStrs, keywordPrefix, keywordSuffix, LevelTypeEnum.CostCenter.getName());
    }

    /**
     * 封装部门类型维度
     *
     * @param typeGroup
     * @param keywordPrefix
     * @param keywordSuffix
     */
    private void buildDepartmentTypeGroup(TypeGroup typeGroup, List<String> filterStrs, String keywordPrefix, String keywordSuffix) {
        List<Department> departments = externalService.queryDepartments();
        List<LevelData> levelDatas = departments.stream().map(v -> {
            LevelData levelData = new LevelData(v.getSerialNo(), v.getName(), v.getDisplayName(), v.getParentSerialNo());
            return levelData;
        }).collect(Collectors.toList());
        buildTypeGroup(levelDatas, typeGroup, filterStrs, keywordPrefix, keywordSuffix, LevelTypeEnum.Department.getName());
    }

    /**
     * 封装支出科目类型维度
     *
     * @param typeGroup
     * @param keywordPrefix
     * @param keywordSuffix
     */
    private void buildSubjectTypeGroup(TypeGroup typeGroup, List<String> filterStrs, String keywordPrefix, String keywordSuffix) {
        List<Subject> subjects = externalService.querySubjects();
        List<LevelData> levelDatas = subjects.stream().map(v -> {
            LevelData levelData = new LevelData(v.getSubjectId(), v.getSubjectName(), v.getSubjectLongName(), v.getSuperiorSubjectsId());
            return levelData;
        }).collect(Collectors.toList());
        buildTypeGroup(levelDatas, typeGroup, filterStrs, keywordPrefix, keywordSuffix, LevelTypeEnum.Subject.getName());
    }

    /**
     * 去掉需要聚合的维度
     *
     * @param groups
     * @param typeGroups
     * @return
     */
    public List<String> groupsFilter(List<String> groups, List<TypeGroup> typeGroups) {
        if (groups != null && groups.size() > 0) {
            groups = groups.stream().filter(group -> !isTypeGroup(group) && !typeGroups.stream().map(t -> t.getColumn()).collect(Collectors.toList()).contains(group)).collect(Collectors.toList());
            groups = groups.stream().distinct().collect(Collectors.toList());

        }
        return groups;
    }

    /**
     * 层级数据过滤条件的转化
     *
     * @param filters
     * @return
     */
    public List<String> filtersFilter(List<String> filters) {
        //去掉成本中心和支出科目根节点过滤
        filters = filters.stream().filter(v -> {
            SqlFilter sqlFilter = JSON.parseObject(v, SqlFilter.class);
            return !"成本中心".equals(sqlFilter.getValue().toString().trim()) && !"支出科目".equals(sqlFilter.getValue().toString().trim());
        }).collect(Collectors.toList());
        return filters;
    }

    public static boolean isTypeGroup(String content) {
        try {
            JSON.parseObject(content, TypeGroup.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
