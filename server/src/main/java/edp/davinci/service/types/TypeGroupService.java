package edp.davinci.service.types;

import edp.davinci.core.common.Constants;
import edp.davinci.core.enums.types.DateTypeEnum;
import edp.davinci.dto.viewDto.TypeGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author linda
 */
@Slf4j
@Component
public class TypeGroupService {
    /**
     * impala 维度聚合运算转换
     *
     * @param typeGroups
     * @param keywordPrefix
     * @param keywordSuffix
     * @return
     */
    public List<TypeGroup> toTypeGroups(List<TypeGroup> typeGroups, String keywordPrefix, String keywordSuffix) {
        if (typeGroups != null && typeGroups.size() > 0) {
            typeGroups = typeGroups.stream().map(typeGroup -> {
                String visualType = typeGroup.getVisualType();
                if ("date".equals(visualType)) {
                    buildDateTypeGroup(typeGroup, keywordPrefix, keywordSuffix);
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
     * 去掉需要聚合的维度
     *
     * @param groups
     * @param typeGroups
     * @return
     */
    public List<String> groupsFilter(List<String> groups, List<TypeGroup> typeGroups) {
        if (typeGroups != null && typeGroups.size() > 0) {
            groups = groups.stream().filter(group -> !typeGroups.stream().map(typeGroup -> typeGroup.getColumn()).collect(Collectors.toList()).contains(group)).collect(Collectors.toList());
        }
        return groups;
    }
}
