package edp.davinci.service.types;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import edp.core.exception.ServerException;
import edp.core.utils.SqlUtils;
import edp.davinci.core.common.Constants;
import edp.davinci.core.enums.SqlOperatorEnum;
import edp.davinci.core.enums.types.DateTypeEnum;
import edp.davinci.core.enums.types.FastCalculateTypeEnum;
import edp.davinci.core.enums.types.LevelTypeEnum;
import edp.davinci.core.model.SqlFilter;
import edp.davinci.dto.viewDto.*;
import edp.davinci.model.Source;
import edp.davinci.model.mdm.CostCenter;
import edp.davinci.model.mdm.Department;
import edp.davinci.model.mdm.LevelData;
import edp.davinci.model.mdm.Subject;
import edp.davinci.service.ExternalService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author linda
 */
@Slf4j
@Component
public class TypeGroupService {

    @Autowired
    private ExternalService externalService;

    @Autowired
    private SqlUtils sqlUtils;

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
            JSONObject modelObj = JSON.parseObject(model);
            for (String group : groups) {
                if (isDateTypeGroup(group)) {
                    String[] groupNames = group.split(Constants.DATE_FLAG);
                    TypeGroup typeGroup = new TypeGroup(groupNames[0], "date", groupNames[1], "");
                    typeGroup.setColumnAlias(group);
                    typeGroups.add(typeGroup);
                } else {
                    Model modelVal = JSON.parseObject(modelObj.getString(group), Model.class);
                    String visualType = modelVal.getVisualType();
                    TypeGroup typeGroup = new TypeGroup(group, visualType, "", "");
                    //初始化时间维度为ymd
                    if ("date".equals(visualType)) {
                        typeGroup.setFormatType(DateTypeEnum.ymd.name());
                    }
                    typeGroups.add(typeGroup);
                }
            }
        }
        typeGroups = typeGroups.stream().filter(typeGroup -> isTypeGroupVisualType(typeGroup.getVisualType())).collect(Collectors.toList());
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
     * 同比环比的计算sql
     *
     * @param st          上次查询的sql
     * @param aggregators 指标聚合
     * @param groups      涉及到的所有维度集合
     * @param typeGroups
     * @param filters
     * @param source
     * @param model
     * @return
     */
    public String buildFastCalculate(ST st, List<Aggregator> aggregators, List<Order> orders, List<String> groups, List<TypeGroup> typeGroups, List<String> filters, Source source, String model) {
        //原始查询接口
        String originSql = st.render();
        Set dateColumn = new HashSet();
        //获取同比环比类型的汇总
        List<String> fastCalculateTypes = aggregators.stream().filter(
                v -> v.getFastCalculateType() != null && !v.getFastCalculateType().isEmpty())
                .map(v -> v.getFastCalculateType())
                .distinct()
                .collect(Collectors.toList());
        //没有同比环比的计算直接返回原始sql
        if (fastCalculateTypes == null || fastCalculateTypes.size() == 0) {
            return originSql;
        }
        //对过滤值进行转换（时间维度的的只能算同比，计算上一年的数据范围）
        filters = filters.stream().map(f -> {
            SqlFilter sqlFilter = JSON.parseObject(f, SqlFilter.class);
            String name = sqlFilter.getName();
            String visualType = sqlFilter.getVisualType();
            if (visualType == null || visualType.isEmpty()) {
                visualType = getColumnVisualType(name, model);
            }
            if ("date".equals(visualType)) {
                dateColumn.add(sqlFilter.getColumn());
                if (fastCalculateTypes.contains(FastCalculateTypeEnum.QOQ.getName())) {
                    throw new ServerException("时间的筛选和环比不能同时存在");
                }
                String operator = sqlFilter.getOperator();
                Object value = sqlFilter.getValue();
                if (SqlOperatorEnum.BETWEEN.getValue().equalsIgnoreCase(operator)) {
                    JSONArray values = (JSONArray) value;
                    values = values.stream().map(v -> getLastYear(v.toString().replaceAll("'", "")))
                            .collect(Collectors.collectingAndThen(Collectors.toCollection(()
                                    -> new JSONArray()), JSONArray::new));
                    sqlFilter.setValue(values);
                }
                if (SqlOperatorEnum.REGEXP.getValue().equalsIgnoreCase(operator)) {
                    sqlFilter.setValue(getLastYear(value.toString()));
                }
            }
            return JSON.toJSONString(sqlFilter);
        }).collect(Collectors.toList());
        //初始化
        //不需要排序
        st.remove("orders");
        st.remove("filters");
        st.add("filters", ViewExecuteParam.convertFilters(filters, source));
        st.remove("valueFilters");
        //过滤有时间维度的字段（同比环比只能至多有一个时间维度字段）
        List<TypeGroup> dateTypeGroups = typeGroups.stream().filter(v -> "date".equals(v.getVisualType())).collect(Collectors.toList());
        for (TypeGroup dateTypeGroup : dateTypeGroups) {
            dateColumn.add(dateTypeGroup.getColumn());
        }
        if (dateColumn.size() > 1) {
            throw new ServerException("同比环比至多只能存在一个日期维度字段");
        }
        String keywordPrefix = sqlUtils.getKeywordPrefix(source.getJdbcUrl(), source.getDbVersion());
        String keywordSuffix = sqlUtils.getKeywordSuffix(source.getJdbcUrl(), source.getDbVersion());
        List<Map> fastCalculates = new ArrayList<>();
        for (String fastCalculateType : fastCalculateTypes) {
            if (dateTypeGroups.size() == 0) {
                String fastCalculateSql = st.render();
                Map fastCalculate = new HashMap(2);
                fastCalculate.put("type", fastCalculateType);
                fastCalculate.put("sql", fastCalculateSql);
                fastCalculates.add(fastCalculate);
                continue;
            }
            TypeGroup dateTypeGroup = dateTypeGroups.get(0);
            String column = dateTypeGroup.getColumn();
            String formatType = dateTypeGroup.getFormatType();
            TypeGroup dateTypeGroupObj = new TypeGroup(column, dateTypeGroup.getVisualType(), formatType + "_" + fastCalculateType, "");
            dateTypeGroupObj.setColumnAlias(dateTypeGroup.getColumnAlias());
            buildDateTypeGroup(dateTypeGroupObj, keywordPrefix, keywordSuffix);
            typeGroups = typeGroups.stream().map(v -> {
                if (v.getColumn().equals(column)) {
                    return dateTypeGroupObj;
                }
                return v;
            }).collect(Collectors.toList());
            st.remove("typeGroups");
            st.add("typeGroups", typeGroups);
            String fastCalculateSql = st.render();
            Map fastCalculate = new HashMap(2);
            fastCalculate.put("type", fastCalculateType);
            fastCalculate.put("sql", fastCalculateSql);
            fastCalculates.add(fastCalculate);
        }
        aggregators = aggregators.stream().collect(//去重
                Collectors.collectingAndThen(Collectors.toCollection(()
                        -> new TreeSet<>(Comparator.comparing(Aggregator::getAlias))), ArrayList::new));


        STGroup stg = new STGroupFile(Constants.SQL_TEMPLATE);
        ST stFastCalculate = stg.getInstanceOf("queryFastCalculateSql");
        stFastCalculate.add("originSql", originSql);
        stFastCalculate.add("fastCalculates", fastCalculates);
        stFastCalculate.add("groups", groups);
        stFastCalculate.add("orders", orders);
        stFastCalculate.add("aggregators", aggregators);
        stFastCalculate.add("keywordPrefix", keywordPrefix);
        stFastCalculate.add("keywordSuffix", keywordSuffix);
        String sql = stFastCalculate.render();
        return sql;
    }

    private String getCalculateTypeByFunc(String func) {
        String pattern = "@(.*)@";
        // 创建 Pattern 对象
        Pattern r = Pattern.compile(pattern);
        // 现在创建 matcher 对象
        Matcher matcher = r.matcher(func);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    /**
     * 封装日期类型维度
     *
     * @param typeGroup
     * @param keywordPrefix
     * @param keywordSuffix
     */
    private void buildDateTypeGroup(TypeGroup typeGroup, String keywordPrefix, String keywordSuffix) {
        String formatType = typeGroup.getFormatType();
        String column = typeGroup.getColumn();
        for (DateTypeEnum dateTypeEnum : DateTypeEnum.values()) {
            if (dateTypeEnum.name().equals(formatType)) {
                ST aggSt = new ST(dateTypeEnum.getAgg());
                aggSt.add("keywordPrefix", keywordPrefix);
                aggSt.add("column", column);
                aggSt.add("keywordSuffix", keywordSuffix);
                if (formatType.indexOf(DateTypeEnum.yq.name()) != -1) {
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
        List<SqlFilter> sqlFilters = filterStrs.stream()
                .map(v -> JSON.parseObject(v, SqlFilter.class))
                .filter(v ->
                        visualType.equals(v.getVisualType())
                                && v.getName().equals(column)
                )
                .collect(Collectors.toList());
        //层级的数据只获取最后一个筛选
        List<SqlFilter> filters = new ArrayList<>();
        if (sqlFilters != null && sqlFilters.size() > 1) {
            int filtersLen = sqlFilters.size();
            SqlFilter filter = sqlFilters.get(filtersLen - 1);
            filters.add(filter);
        } else {
            filters = sqlFilters;
        }
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
            List<String> values = Arrays.asList(sqlFilter.getValue().toString().split("\\|"));
            boolean isCustomFilter = sqlFilter.isCustomFilter();
            //自定义筛选值（要模糊匹配叶子节点）
            if (isCustomFilter) {
                String pattern = values.stream().map(v -> ".*" + v + ".*").collect(Collectors.joining("|"));
                List<LevelData> levelVals = levelDatas.stream().filter(v -> Pattern.matches(pattern, v.getName())).collect(Collectors.toList());
                //只取最上级的层级，下属层级去掉
                List<LevelData> levelValss = levelVals.stream().filter((v -> checkedValFilter(v.getLongName(), levelVals))).collect(Collectors.toList());
                values = levelValss.stream().map((v -> v.getLongName())).collect(Collectors.toList());
                //回设新的正则表达式
                sqlFilter.setValue(levelValss.stream().map(v -> v.getLongName()).collect(Collectors.joining("|")));
                filters.set(0, sqlFilter);
            }
            //过滤条件下只有一个节点（展示下一个下一个节点的数据）
            if (values.size() == 1 && sqlFilter.isLevelFilter() && !isCustomFilter) {
                //获取当前节点
                List<String> finalValues = values;
                List<LevelData> rootLevels = levelDatas.stream().filter(v -> finalValues.get(0).equals(v.getLongName())).collect(Collectors.toList());
                //只有一个的情况下，寻找下级节点
                if (rootLevels.size() == 1) {
                    LevelData levelData = rootLevels.get(0);
                    //获取当前节点下一级节点
                    nextLevel = levelDatas.stream().filter(v -> v.getParentId() != null && v.getParentId().equals(levelData.getId())).map(v -> v.getLongName()).collect(Collectors.toList());
                    //当前节点也要加上
                    nextLevel.add(0, rootLevels.get(0).getLongName() + "$");
                } else if (rootLevels.size() > 1) {
                    nextLevel = rootLevels.stream().map(v -> v.getLongName()).collect(Collectors.toList());
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
        //获取过滤条件的所有值
        List<Object> values = filters.stream().map(v -> {
                    String value = v.getValue() + "";
                    String operator = v.getOperator();
                    if ("regexp".equals(operator)) {
                        value = levelRegexpFilter(value);
                    }
                    return value;
                }
        ).collect(Collectors.toList());
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
     * 校验是否包含子节点
     *
     * @param lastLongName 上一级部门长路径
     * @param values
     */
    private boolean checkedValFilter(String lastLongName, List<LevelData> values) {
        List<LevelData> checks = values.stream().filter(v
                -> lastLongName.indexOf(v.getLongName() + "-") == 0 && lastLongName != v.getLongName()
        ).collect(Collectors.toList());
        return checks.size() == 0;
    }


    private String levelRegexpFilter(String value) {
        value = value.replaceAll("\\(", "\\\\\\\\(").replaceAll("\\)", "\\\\\\\\)");
        return value;
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
     * @param isFilterisTypeGroup 是否过滤TypeGroup
     * @return
     */
    public List<String> groupsFilter(List<String> groups, List<TypeGroup> typeGroups, boolean isFilterisTypeGroup) {
        if (groups != null && groups.size() > 0) {
            if (isFilterisTypeGroup) {
                groups = groups.stream().map(v -> {
                    if (isDateTypeGroup(v)) {
                        return v.split(Constants.DATE_FLAG)[0];
                    }
                    return v;
                }).collect(Collectors.toList());
                groups = groups.stream().filter(group -> !isTypeGroup(group) && !typeGroups.stream().map(t -> t.getColumn()).collect(Collectors.toList()).contains(group)).collect(Collectors.toList());
            }
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
        //去掉成本中心和支出科目根节点过滤（因为根节点是面向所有数据的=不加任何过滤条件）
        filters = filters.stream().filter(v -> {
            SqlFilter sqlFilter = JSON.parseObject(v, SqlFilter.class);
            return !"成本中心".equals(sqlFilter.getValue().toString().trim())
                    && !"支出科目".equals(sqlFilter.getValue().toString().trim())
                    //日期类型（可能格式化后的），需要放到最外层去过滤，放到里面会找不到
                    && !sqlFilter.isOutSideFilter()
                    ;
        }).map(v -> {
            SqlFilter sqlFilter = JSON.parseObject(v, SqlFilter.class);
            String operator = sqlFilter.getOperator();
            String value = sqlFilter.getValue() + "";
            if ("regexp".equals(operator)) {
                sqlFilter.setValue(levelRegexpFilter(value));
            }
            return JSON.toJSONString(sqlFilter);
        }).collect(Collectors.toList());
        return filters;
    }

    /**
     * 日期数据过滤条件的获取
     *
     * @param filters
     * @return
     */
    public List<String> valueFiltersFilter(List<String> filters) {
        //去掉成本中心和支出科目根节点过滤
        filters = filters.stream().filter(v -> {
            SqlFilter sqlFilter = JSON.parseObject(v, SqlFilter.class);
            //只取日期类型的过滤，放到最外层做过滤，相应的filtersFilter需要去掉
            return sqlFilter.isOutSideFilter();
        }).collect(Collectors.toList());
        return filters;
    }

    /**
     * @param aggregators
     * @return
     */
    public List<Aggregator> aggregatorsFilter(List<Aggregator> aggregators) {
        aggregators = aggregators.stream().map(v -> {
            if (v.getFastCalculateType() == null || v.getFastCalculateType().isEmpty()) {
                String type = getCalculateTypeByFunc(v.getFunc());
                if (isFastCalculateType(type)) {
                    v.setFastCalculateType(type);
                }
            }
            return v;
        }).collect(Collectors.toList());
        return aggregators;
    }

    /**
     * 是否需要在最外层过滤
     *
     * @param visualType
     * @return
     */
    private boolean isOutSideFilter(String visualType) {
        return ("date".equals(visualType));
    }

    /**
     * 排序数据的转化
     *
     * @param orders
     * @return
     */
    public List<Order> ordersFilter(List<Order> orders) {
        if (orders == null) {
            return orders;
        }
        orders = orders.stream().map(v -> {
            String column = v.getColumn();
            if (isDateTypeGroup(column)) {
                v.setColumn(column.split(Constants.DATE_FLAG)[0]);
            }
            return v;
        }).collect(Collectors.toList());
        return orders;
    }

    public static boolean isTypeGroup(String content) {
        try {
            JSON.parseObject(content, TypeGroup.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isDateTypeGroup(String content) {
        return content.indexOf(Constants.DATE_FLAG) != -1;
    }

    public static String getColumnVisualType(String column, String model) {
        if (column.indexOf(Constants.DATE_FLAG) != -1) {
            return "date";
        }
        JSONObject modelObj = JSON.parseObject(model);
        Model modelVal = JSON.parseObject(modelObj.getString(column), Model.class);
        return modelVal.getVisualType();
    }

    /**
     * 是否是层级类型数据
     *
     * @param visualType
     * @return
     */
    public static boolean isLevelVisualType(String visualType) {
        List<LevelTypeEnum> levelTypeEnums = Arrays.asList(LevelTypeEnum.values());
        return levelTypeEnums.stream().filter(v -> v.getName().equals(visualType)).collect(Collectors.toList()).size() > 0;
    }

    /**
     * 是否是typeGroup类型数据
     *
     * @param visualType
     * @return
     */
    public static boolean isTypeGroupVisualType(String visualType) {
        return isLevelVisualType(visualType) || "date".equals(visualType);
    }

    /**
     * 是否是快速计算类型数据
     *
     * @param type
     * @return
     */
    public static boolean isFastCalculateType(String type) {
        if (type == null || type.isEmpty()) {
            return false;
        }
        List<FastCalculateTypeEnum> fastCalculateTypeEnums = Arrays.asList(FastCalculateTypeEnum.values());
        return fastCalculateTypeEnums.stream().filter(v -> v.getName().equals(type)).collect(Collectors.toList()).size() > 0;
    }


    public static String getDateFormatStr(String date) {
        String dateFormat = null;
        for (String str : Constants.DATE_FORMATS) {
            SimpleDateFormat format = new SimpleDateFormat(str);
            try {
                format.setLenient(false);
                format.parse(date);
                dateFormat = str;
                break;
            } catch (ParseException e) {
                continue;
            }

        }
        return dateFormat;
    }

    private String getLastYear(String date) {
        String dateFormat = getDateFormatStr(date);
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        Calendar c = Calendar.getInstance();
        //过去一年
        try {
            c.setTime(format.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.add(Calendar.YEAR, -1);
        return format.format(c.getTime());
    }

}
