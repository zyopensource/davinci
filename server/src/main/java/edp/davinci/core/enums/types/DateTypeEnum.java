package edp.davinci.core.enums.types;

/**
 * @author linda
 */

public enum DateTypeEnum {

    /**
     *
     */
    YMD("from_timestamp(<keywordPrefix><column><keywordSuffix>,'yyyy/MM/dd')"),
    YM("from_timestamp(<keywordPrefix><column><keywordSuffix>,'yyyy/MM')"),
    Y(" from_timestamp(<keywordPrefix><column><keywordSuffix>,'yyyy')"),
    YQ("trunc(<keywordPrefix><column><keywordSuffix>,'Q')"),
    YW("CONCAT(cast(year(<keywordPrefix><column><keywordSuffix>) as string),'-W',cast(weekofyear(<keywordPrefix><column><keywordSuffix>) as string))"),
    ;
    private String agg;

    DateTypeEnum(String agg) {
        this.agg = agg;
    }

    public String getAgg() {
        return agg;
    }
}
