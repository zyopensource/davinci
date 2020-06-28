package edp.davinci.core.enums.types;

/**
 * @author linda
 */

public enum DateTypeEnum {

    /**
     *
     */
    ymd("from_timestamp(<keywordPrefix><column><keywordSuffix>,'yyyy/MM/dd')"),
    ym("from_timestamp(<keywordPrefix><column><keywordSuffix>,'yyyy/MM')"),
    y(" from_timestamp(<keywordPrefix><column><keywordSuffix>,'yyyy')"),
    yq("trunc(<keywordPrefix><column><keywordSuffix>,'Q')"),
    yw("CONCAT(cast(year(<keywordPrefix><column><keywordSuffix>) as string),'-W',cast(weekofyear(<keywordPrefix><column><keywordSuffix>) as string))"),
    ;
    private String agg;

    DateTypeEnum(String agg) {
        this.agg = agg;
    }

    public String getAgg() {
        return agg;
    }
}
