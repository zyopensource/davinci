/*
 * <<
 *  Davinci
 *  ==
 *  Copyright (C) 2016 - 2019 EDP
 *  ==
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  >>
 *
 */

package edp.davinci.dto.viewDto;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import edp.core.utils.CollectionUtils;
import edp.core.utils.SqlUtils;
import edp.davinci.core.common.Constants;
import edp.davinci.core.model.SqlFilter;
import edp.davinci.model.Source;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

import static edp.core.consts.Consts.*;

@Data
@Slf4j
public class ViewExecuteParam {
    private List<String> groups;
    private List<Aggregator> aggregators;
    private List<Order> orders;
    private List<String> filters;
    private List<Param> params;
    private Boolean cache;
    private Long expired;
    private Boolean flush = false;
    private int limit = 0;
    private int pageNo = -1;
    private int pageSize = -1;
    private int totalCount = 0;

    private boolean nativeQuery = false;


    public ViewExecuteParam() {
    }

    public ViewExecuteParam(List<String> groupList,
                            List<Order> orders,
                            List<String> filterList,
                            List<Param> params,
                            Boolean cache,
                            Long expired,
                            Boolean nativeQuery) {
        this.groups = groupList;
        this.orders = orders;
        this.filters = filterList;
        this.params = params;
        this.cache = cache;
        this.expired = expired;
        this.nativeQuery = nativeQuery;
    }

    public List<String> getGroups() {
        if (!CollectionUtils.isEmpty(this.groups)) {
            this.groups = groups.stream().filter(g -> !StringUtils.isEmpty(g)).collect(Collectors.toList());
        }

        if (CollectionUtils.isEmpty(this.groups)) {
            return null;
        }

        return this.groups;
    }

    public List<String> getFilters() {
        if (!CollectionUtils.isEmpty(this.filters)) {
            this.filters = filters.stream().filter(f -> !StringUtils.isEmpty(f)).collect(Collectors.toList());
        }

        if (CollectionUtils.isEmpty(this.filters)) {
            return null;
        }

        return this.filters;
    }

    public List<Order> getOrders(String jdbcUrl, String dbVersion) {
        List<Order> list = null;
        if (!CollectionUtils.isEmpty(orders)) {
            list = new ArrayList<>();
            String prefix = SqlUtils.getKeywordPrefix(jdbcUrl, dbVersion);
            String suffix = SqlUtils.getKeywordSuffix(jdbcUrl, dbVersion);

            for (Order order : this.orders) {
                String column = order.getColumn().trim();
//                Matcher matcher = PATTERN_SQL_AGGREGATE.matcher(order.getColumn().trim().toLowerCase());
//                if (!matcher.find()) {
                StringBuilder columnBuilder = new StringBuilder();
                if (!column.startsWith(prefix)) {
                    columnBuilder.append(prefix);
                }
                columnBuilder.append(column);
                if (!column.endsWith(suffix)) {
                    columnBuilder.append(suffix);
                }
                order.setColumn(columnBuilder.toString());
//                }
                list.add(order);
            }
        }
        return list;
    }

    public void addExcludeColumn(Set<String> excludeColumns, String jdbcUrl, String dbVersion) {
        if (!CollectionUtils.isEmpty(excludeColumns) && !CollectionUtils.isEmpty(aggregators)) {
            excludeColumns.addAll(this.aggregators.stream()
                    .filter(a -> !CollectionUtils.isEmpty(excludeColumns) && excludeColumns.contains(a.getColumn()))
                    .map(a -> formatColumn(a.getColumn(), a.getFunc(), jdbcUrl, dbVersion, true))
                    .collect(Collectors.toSet())
            );
        }
    }

    public List<Aggregator> getAggregators(String jdbcUrl, String dbVersion) {
        if (!CollectionUtils.isEmpty(aggregators)) {
            return this.aggregators.stream().map(a -> {
                String func = a.getFunc();
                if (a.getFastCalculateType() != null) {
                    func = func.replace("@"+a.getFastCalculateType()+"@","");
                }
                String formatColumn = formatColumn(a.getColumn(), func, jdbcUrl, dbVersion, false);
                a.setColumn(formatColumn);
                String[] formatColumns = formatColumn.split("AS");
                String alias = formatColumns[1].trim().replaceAll("'", "");
                if (a.getFastCalculateType() != null) {
                    alias = alias.replaceFirst(func,func+"@"+a.getFastCalculateType()+"@") ;
                    a.setColumn(formatColumns[0]+" AS '"+alias+"'");
                }
                a.setAlias(alias);
                return a;
            }).collect(//去重
                    Collectors.collectingAndThen(Collectors.toCollection(()
                            -> new TreeSet<>(Comparator.comparing(Aggregator::getAlias))), ArrayList::new));
        }
        return null;
    }


    private String formatColumn(String column, String func, String jdbcUrl, String dbVersion, boolean isLable) {
        if (isLable) {
            return String.join(EMPTY, func.trim(), PARENTHESES_START, column.trim(), PARENTHESES_END);
        } else {
            StringBuilder sb = new StringBuilder();
            String field = ViewExecuteParam.getField(column, jdbcUrl, dbVersion);
            if ("COUNTDISTINCT".equals(func.trim().toUpperCase())) {
                sb.append("COUNT").append(PARENTHESES_START).append("DISTINCT").append(SPACE);
                sb.append(field);
                sb.append(PARENTHESES_END);
                sb.append(" AS ").append(SqlUtils.getAliasPrefix(jdbcUrl, dbVersion)).append("COUNTDISTINCT").append(PARENTHESES_START);
                sb.append(column);
                sb.append(PARENTHESES_END).append(SqlUtils.getAliasSuffix(jdbcUrl, dbVersion));
            }
            else if(func.trim().toUpperCase().contains(Constants.CALCULATE_FLAG.toUpperCase())){
                sb.append(func.trim().replace(Constants.CALCULATE_FLAG,PARENTHESES_START+field+PARENTHESES_END));
                sb.append(" AS ").append(SqlUtils.getAliasPrefix(jdbcUrl, dbVersion));
                sb.append(func.trim()).append(PARENTHESES_START);
                sb.append(column);
                sb.append(PARENTHESES_END).append(SqlUtils.getAliasSuffix(jdbcUrl, dbVersion));
            }
            else {
                sb.append(func.trim()).append(PARENTHESES_START);
                sb.append(field);
                sb.append(PARENTHESES_END);
                sb.append(" AS ").append(SqlUtils.getAliasPrefix(jdbcUrl, dbVersion));
                sb.append(func.trim()).append(PARENTHESES_START);
                sb.append(column);
                sb.append(PARENTHESES_END).append(SqlUtils.getAliasSuffix(jdbcUrl, dbVersion));
            }
            return sb.toString();
        }
    }

    public static String getField(String field, String jdbcUrl, String dbVersion) {
        String keywordPrefix = SqlUtils.getKeywordPrefix(jdbcUrl, dbVersion);
        String keywordSuffix = SqlUtils.getKeywordSuffix(jdbcUrl, dbVersion);
        if (!StringUtils.isEmpty(keywordPrefix) && !StringUtils.isEmpty(keywordSuffix)) {
            return keywordPrefix + field + keywordSuffix;
        }
        return field;
    }

    public static List<String> convertFilters(List<String> filterStrs, Source source) {
        List<String> whereClauses = new ArrayList<>();
        List<SqlFilter> filters = new ArrayList<>();
        try {
            if (null == filterStrs || filterStrs.isEmpty()) {
                return null;
            }

            for (String str : filterStrs) {
                SqlFilter obj = JSON.parseObject(str, SqlFilter.class);
                if (!StringUtils.isEmpty(obj.getName())) {
                    obj.setName(ViewExecuteParam.getField(obj.getName(), source.getJdbcUrl(), source.getDbVersion()));
                }
                filters.add(obj);
            }
            filters.forEach(filter -> whereClauses.add(SqlFilter.dealFilter(filter)));

        } catch (Exception e) {
            log.error("convertFilters error . filterStrs = {}, source = {}, filters = {} , whereClauses = {} ",
                    JSON.toJSON(filterStrs), JSON.toJSON(source), JSON.toJSON(filters), JSON.toJSON(whereClauses));
            throw e;
        }
        return whereClauses;
    }
}
