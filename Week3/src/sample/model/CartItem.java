package sample.model;



public class CartItem {

    private final Medicine medicine;
    private int quantity;

    public CartItem(
            Medicine medicine,
            int quantity) {

        this.medicine = medicine;
        this.quantity = quantity;
    }

    public Medicine getMedicine() {

        return medicine;
    }

    public String getName() {

        return medicine.getName();
    }

    public double getUnitPrice() {

        return medicine.getPrice();
    }

    public int getQuantity() {

        return quantity;
    }

    public double getSubtotal() {

        return medicine.getPrice()
                * quantity;
    }

    public void increaseQuantity(
            int amount) {

        quantity += amount;
    }
}

// احنا ليه عاملين ميثود بترجع الدواء في ملف الكارت  زليه عاملين ال متغير بتاع الدواء فاينل