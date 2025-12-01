import java.text.SimpleDateFormat;
import java.util.Date;

public class Transaction {
    private final Date date;
    private final String category;
    private final String amount;
    private final int transactionType;
    private final double balance;
    private double totalAmount;

    public Transaction(Date date, String category, String amount, int transactionType, double balance) {
        this.date = date;
        this.category = category;
        this.amount = amount;
        this.transactionType = transactionType;
        this.balance = balance;
    }

    public Date getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public String getAmount() {
        return amount;
    }

    public int getTransactionType() {
        return transactionType;
    }

    public double getBalance() {
        return balance;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String toFileString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String transactionTypeString = (transactionType == 1) ? "Expense" : "Income";

        return dateFormat.format(date) + "\t" +
                transactionTypeString + "\t\t" +
                category + "\t" +
                String.format("%.2f", Double.parseDouble(amount)) + "\t" +
                String.format("%.2f", balance) + "\t" +
                String.format("%.2f", totalAmount);
    }
}
