import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReportGenerator {

    static Product[] productCatalog = new Product[10];

    static class TaskRunnable implements Runnable {
        private final String path;
        private double totalCost;
        private int totalAmount;
        private int totalDiscountSum;
        private int totalLines;
        private Product mostExpensiveProduct;
        private double highestCostAfterDiscount;
        private String mostExpensivePurchaseDetail;


        public TaskRunnable(String path) {
            this.path = path;
            this.totalCost = 0;
            this.totalAmount = 0;
            this.totalDiscountSum = 0;
            this.totalLines = 0;
            this.highestCostAfterDiscount = 0;
            this.mostExpensiveProduct = null;
        }

        @Override
        public void run() {
            try (InputStream is = ReportGenerator.class.getResourceAsStream("/" + path);
                 BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = br.readLine()) != null) {
                    totalLines++;
                    String[] parts = line.split(",");
                    if (parts.length != 3) continue;

                    try {
                        int productId = Integer.parseInt(parts[0].trim());
                        int amount = Integer.parseInt(parts[1].trim());
                        int discount = Integer.parseInt(parts[2].trim());

                        if (productId < 1 || productId > 9) continue;

                        Product product = null;
                        for (Product p : productCatalog) {
                            if (p != null && p.getProductID() == productId) {
                                product = p;
                                break;
                            }
                        }
                        if (product == null) continue;

                        double totalPrice = product.getPrice() * amount;
                        double discountInDollars = (totalPrice * discount) / 100.0;
                        double discountedTotal = totalPrice - discountInDollars;

                        totalCost += discountedTotal;
                        totalAmount += amount;
                        totalDiscountSum += discount;

                        if (discountedTotal > highestCostAfterDiscount) {
                            highestCostAfterDiscount = discountedTotal;
                            mostExpensiveProduct = product;
                            mostExpensivePurchaseDetail = product.getProductName() + " (Cost: $" + String.format("%.2f", discountedTotal) + ")";
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid data in " + path + ": " + line);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading file " + path + ": " + e.getMessage());
            }
        }

        public void makeReport() {
            double averageDiscount = totalLines > 0 ? (double) totalDiscountSum / totalLines : 0;
            System.out.println("\nReport for file: " + path);
            System.out.println("Total cost: $" + String.format("%.2f", totalCost));
            System.out.println("Total items bought: " + totalAmount);
            System.out.println("Average discount: " + String.format("%.2f", averageDiscount) + "%");
            if (mostExpensivePurchaseDetail != null) {
                System.out.println("Most expensive purchase after discount: " + mostExpensivePurchaseDetail);
            } else {
                System.out.println("No expensive purchase recorded.");
            }
        }
    }

    static class Product {
        private final int productID;
        private final String productName;
        private final double price;

        public Product(int productID, String productName, double price) {
            this.productID = productID;
            this.productName = productName;
            this.price = price;
        }

        public int getProductID() {
            return productID;
        }

        public String getProductName() {
            return productName;
        }

        public double getPrice() {
            return price;
        }
    }
    private static final String[] ORDER_FILES = {
            "2021_order_details.txt",
            "2022_order_details.txt",
            "2023_order_details.txt",
            "2024_order_details.txt"
    };


    public static void loadProducts() throws IOException {
        InputStream is = ReportGenerator.class.getResourceAsStream("/Products.txt");
        if (is == null) {
            throw new IOException("Product file not found: /Products.txt");
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            int index = 0;
            while ((line = br.readLine()) != null && index < productCatalog.length) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    try {
                        int productId = Integer.parseInt(parts[0].trim());
                        String productName = parts[1].trim();
                        double price = Double.parseDouble(parts[2].trim());
                        productCatalog[index++] = new Product(productId, productName, price);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid number format in Products.txt: " + line);
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        try {
            loadProducts();

            List<TaskRunnable> tasks = new ArrayList<>();
            List<Thread> threads = new ArrayList<>();

            // Start all threads
            for (String file : ORDER_FILES) {
                TaskRunnable task = new TaskRunnable(file);
                Thread thread = new Thread(task);
                tasks.add(task);
                threads.add(thread);
                thread.start();
            }

            // Waiting for all the threads to finish.
            for (Thread thread : threads) {
                thread.join();
            }

            // Generate reports
            for (TaskRunnable task : tasks) {
                task.makeReport();
            }
        } catch (IOException e) {
            System.err.println("Error loading products: " + e.getMessage());
        }
    }
}