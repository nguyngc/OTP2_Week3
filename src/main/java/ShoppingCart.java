import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import services.LocalizationService;

public class ShoppingCart {
    private static double total = 0.0;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
        System.out.println("Select a language:");
        System.out.println("1. English");
        System.out.println("2. Finnish");
        System.out.println("3. Swedish");
        System.out.println("4. Japanese");
        System.out.println("5. Arabic");
        System.out.print("> ");
        int choice = scanner.nextInt();

        Locale locale;
        switch (choice) {
            case 1:
                locale = new Locale("en", "US");
                break;
            case 2:
                locale = new Locale("fi", "FI");
                break;
            case 3:
                locale = new Locale("sv", "SE");
                break;
            case 4:
                locale = new Locale("ja", "JP");
                break;
            case 5:
                locale = new Locale("ar", "AR");
                break;
            default:
                locale = new Locale("en", "US");
                break;
        }

        Map<String, String> localizedStrings = LocalizationService.getLocalizedStrings(locale);

        System.out.println(localizedStrings.getOrDefault("prompt.items", "Enter number of items:"));
        int items = scanner.nextInt();

        for (int i = 1; i <= items; i++) {
            System.out.println(localizedStrings.getOrDefault("prompt.item_price", "Enter price for item") + " " + i + ":");
            double itemPrice = scanner.nextDouble();

            System.out.println(localizedStrings.getOrDefault("prompt.item_quantity", "Enter quantity for item") + " " + i + ":");
            double itemQuantity = scanner.nextDouble();

            double itemCost = calculateItemCost(itemPrice, itemQuantity);
            total = calculateTotal(itemCost);
        }
        System.out.println(localizedStrings.getOrDefault("result", "Total:") + " " + total);
    }

    public static double calculateItemCost(double itemPrice, double itemQuantity) {
        return itemPrice * itemQuantity;
    }

    public static double calculateTotal(double itemCost) {
        return total += itemCost;
    }
}
