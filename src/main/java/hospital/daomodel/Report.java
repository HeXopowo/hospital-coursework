package hospital.daomodel;

public class Report {
    private String category;
    private int count;
    private String additionalInfo;
    private String extra1;
    private String extra2;

    public Report() {}

    public Report(String category, int count, String additionalInfo) {
        this(category, count, additionalInfo, null, null);
    }

    public Report(String category, int count, String additionalInfo,
                     String extra1, String extra2) {
        this.category = category;
        this.count = count;
        this.additionalInfo = additionalInfo;
        this.extra1 = extra1;
        this.extra2 = extra2;
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public String getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }

    public String getExtra1() { return extra1; }
    public void setExtra1(String extra1) { this.extra1 = extra1; }

    public String getExtra2() { return extra2; }
    public void setExtra2(String extra2) { this.extra2 = extra2; }
}