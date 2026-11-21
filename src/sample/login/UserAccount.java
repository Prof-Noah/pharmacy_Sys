package sample.login;

public class UserAccount {

    private final String username;
    private final String pharmacyName;
    private final int lowStockThreshold;

    public UserAccount(String username, String pharmacyName, int lowStockThreshold) {
        this.username = username;
        this.pharmacyName = pharmacyName;
        this.lowStockThreshold = lowStockThreshold;
    }

    public String getUsername() {
        return username;
    }

    public String getPharmacyName() {
        return pharmacyName;
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }
}
