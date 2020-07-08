package edp.davinci.core.enums.types;

/**
 * @author linda
 */

public enum DateTypeEnum {

    /**
     * 日期类型转换
     */
    ymd("from_timestamp(<keywordPrefix><column><keywordSuffix>,'yyyy-MM-dd')"),
    ym("from_timestamp(<keywordPrefix><column><keywordSuffix>,'yyyy-MM')"),
    y(" from_timestamp(<keywordPrefix><column><keywordSuffix>,'yyyy')"),
    yq("trunc(<keywordPrefix><column><keywordSuffix>,'Q')"),
    yw("CONCAT(cast(year(<keywordPrefix><column><keywordSuffix>) as string),'-W',cast(weekofyear(<keywordPrefix><column><keywordSuffix>) as string))"),

    /**
     *同比日期类型转换
     */
    ymd_yoy("from_timestamp(years_add(<keywordPrefix><column><keywordSuffix>,1),'yyyy-MM-dd')"),
    ym_yoy("from_timestamp(years_add(<keywordPrefix><column><keywordSuffix>,1),'yyyy-MM')"),
    y_yoy(" from_timestamp(years_add(<keywordPrefix><column><keywordSuffix>,1),'yyyy')"),
    yq_yoy("trunc(years_add(<keywordPrefix><column><keywordSuffix>,1),'Q')"),
    yw_yoy("CONCAT(cast(year(years_add(<keywordPrefix><column><keywordSuffix>,1)) as string),'-W',cast(weekofyear(years_add(<keywordPrefix><column><keywordSuffix>,1)) as string))"),
    /**
     *环比日期类型转换
     */
    ymd_qoq("from_timestamp(date_add(<keywordPrefix><column><keywordSuffix>,1),'yyyy-MM-dd')"),
    ym_qoq("from_timestamp(months_add(<keywordPrefix><column><keywordSuffix>,1),'yyyy-MM')"),
    y_qoq(" from_timestamp(years_add(<keywordPrefix><column><keywordSuffix>,1),'yyyy')"),
    yq_qoq("trunc(months_add(<keywordPrefix><column><keywordSuffix>,3),'Q')"),
    yw_qoq("CONCAT(cast(year(weeks_add(<keywordPrefix><column><keywordSuffix>,1)) as string),'-W',cast(weeks_add(years_add(<keywordPrefix><column><keywordSuffix>,1)) as string))"),
    ;
    private String agg;

    DateTypeEnum(String agg) {
        this.agg = agg;
    }

    public String getAgg() {
        return agg;
    }
}
