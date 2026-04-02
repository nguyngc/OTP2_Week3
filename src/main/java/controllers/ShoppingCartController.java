package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import services.CartService;
import services.LocalizationService;

import java.text.NumberFormat;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.util.Map;

public class ShoppingCartController {
    private static final CartService CART_SERVICE = new CartService();

    @FXML
    private VBox rootVBox;

    @FXML
    private Label lblLanguage;

    @FXML
    private ComboBox<String> cbLanguage;

    @FXML
    private Label lblItemCount;

    @FXML
    private TextField txtItemCount;

    @FXML
    private Button btnGenerate;

    @FXML
    private Button btnCalculate;

    @FXML
    private VBox itemsContainer;

    @FXML
    private Label lblTotal;

    private Locale currentLocale = new Locale("en", "US");
    private Map<String, String> localizedStrings;

    @FXML
    public void initialize() {
        // Set initial language
        setLanguage(currentLocale);

        cbLanguage.getItems().addAll("English", "Finnish", "Swedish", "Japanese", "Arabic");
        cbLanguage.setValue("English");

        cbLanguage.setOnAction(e -> {
            txtItemCount.clear();
            String currentLanguage = cbLanguage.getValue();
            switch (currentLanguage) {
                case "English":
                    currentLocale = new Locale("en", "US");
                    break;
                case "Finnish":
                    currentLocale = new Locale("fi", "FI");
                    break;
                case "Swedish":
                    currentLocale = new Locale("sv", "SE");
                    break;
                case "Japanese":
                    currentLocale = new Locale("ja", "JP");
                    break;
                case "Arabic":
                    currentLocale = new Locale("ar", "AR");
                    break;
                default:
                    currentLocale = new Locale("en", "US");
                    break;
            }

            setLanguage(currentLocale);
        });
    }

    /**
     * Set the application language
     */
    private void setLanguage(Locale locale) {
        lblTotal.setText(""); // Clear previous result
        itemsContainer.getChildren().clear();

        // Load localized strings
        localizedStrings = LocalizationService.getLocalizedStrings(locale);

        // Update all UI text
        updateTexts();

        // Apply text direction based on language
        applyTextDirection(locale);
    }

    private void updateTexts() {
        lblLanguage.setText(localizedStrings.getOrDefault("label.language", "Select the language:"));
        lblItemCount.setText(localizedStrings.getOrDefault("prompt.items", "Enter number of items:"));
        btnGenerate.setText(localizedStrings.getOrDefault("button.generate", "Enter items"));
        btnCalculate.setText(localizedStrings.getOrDefault("button.calculate", "Calculate Total"));
        lblTotal.setText(localizedStrings.getOrDefault("result", "Total: ") + " " + formatCurrency(0.0));
        txtItemCount.setPromptText(localizedStrings.getOrDefault("prompt.items", "Enter number of items:"));
    }

    @FXML
    private void handleGenerate() {
        itemsContainer.getChildren().clear();

        int itemCount;
        try {
            itemCount = Integer.parseInt(txtItemCount.getText().trim());
            if (itemCount <= 0) {
                showAlert(localizedStrings.getOrDefault("error.invalid_number", "Please enter a valid number of items."));
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(localizedStrings.getOrDefault("error.invalid_number", "Please enter a valid number of items."));
            return;
        }

        for (int i = 1; i <= itemCount; i++) {
            itemsContainer.getChildren().add(createItemRow(i));
        }
    }

    private HBox createItemRow(int index) {
        Label lblPrice = new Label(localizedStrings.getOrDefault("prompt.item_price", "Enter price for item") + " " + index + ":");
        TextField txtPrice = new TextField();
        txtPrice.setPromptText(localizedStrings.getOrDefault("prompt.item_price", "Enter price for item") + " " + index);
        txtPrice.setPrefWidth(100);

        Label lblQuantity = new Label(localizedStrings.getOrDefault("prompt.item_quantity", "Enter quantity for item") + " " + index + ":");
        TextField txtQuantity = new TextField();
        txtQuantity.setPromptText(localizedStrings.getOrDefault("prompt.item_quantity", "Enter quantity for item") + " " + index);
        txtQuantity.setPrefWidth(100);

        Label lblItemCost = new Label(localizedStrings.getOrDefault("result", "Total") + ": " + formatCurrency(0.0));
        lblItemCost.setPrefWidth(140);

        HBox row = new HBox(10, lblPrice, txtPrice, lblQuantity, txtQuantity, lblItemCost);
        row.setUserData(new ItemRowData(lblPrice, txtPrice, lblQuantity, txtQuantity, lblItemCost, index));
        return row;
    }

    @FXML
    private void handleCalculate() {
        double total = 0.0;
        List<CartService.CartItem> cartItems = new ArrayList<>();

        try {
            for (var node : itemsContainer.getChildren()) {
                if (node instanceof HBox row && row.getUserData() instanceof ItemRowData data) {
                    double price = Double.parseDouble(data.txtPrice.getText().trim());
                    int quantity = Integer.parseInt(data.txtQuantity.getText().trim());

                    double itemTotal = price * quantity;
                    total += itemTotal;
                    cartItems.add(new CartService.CartItem(data.index, price, quantity, itemTotal));

                    data.lblTotal.setText(localizedStrings.getOrDefault("result", "Total: ") + " " + formatCurrency(itemTotal));
                }
            }

            lblTotal.setText(localizedStrings.getOrDefault("result", "Total: ") + " " + formatCurrency(total));
            saveCart(cartItems, total);

        } catch (NumberFormatException e) {
            showAlert(localizedStrings.getOrDefault("error.invalid_input", "Please enter valid price and quantity values."));
        }
    }

    private void saveCart(List<CartService.CartItem> cartItems, double total) {
        try {
            CART_SERVICE.saveCart(cartItems.size(), total, LocalizationService.toLanguageCode(currentLocale), cartItems);
        } catch (SQLException exception) {
            showAlert(localizedStrings.getOrDefault(
                    "error.database",
                    "Database operation failed. Please check your database connection."
            ));
        }
    }

    private String formatCurrency(double amount) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(getCurrencyLocale());
        return currencyFormat.format(amount);
    }

    private Locale getCurrencyLocale() {
        if ("ar".equals(currentLocale.getLanguage())) {
            return new Locale("ar", "AR");
        }
        return currentLocale;
    }

    /**
     * Apply LTR or RTL layout direction
     */
    private void applyTextDirection(Locale locale) {
        // Step 1: Detect if the language is RTL
        String lang = locale.getLanguage();
        boolean isRTL = lang.equals("fa")   // Persian
                || lang.equals("ur")   // Urdu
                || lang.equals("ar")   // Arabic
                || lang.equals("he");  // Hebrew

        // Step 2: Wrap UI changes in Platform.runLater() for thread safety
        Platform.runLater(() -> {
            // Step 3: Set NodeOrientation on the root VBox
            if (rootVBox != null) {
                rootVBox.setNodeOrientation(
                        isRTL ? NodeOrientation.RIGHT_TO_LEFT
                                : NodeOrientation.LEFT_TO_RIGHT
                );
            }
        });
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(localizedStrings.getOrDefault("error.title", "Error"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static double calculateItemCost(double itemPrice, double itemQuantity) {
        return itemPrice * itemQuantity;
    }

    private static class ItemRowData {
        Label lblPrice;
        TextField txtPrice;
        Label lblQuantity;
        TextField txtQuantity;
        Label lblTotal;
        int index;

        ItemRowData(Label lblPrice, TextField txtPrice,
                    Label lblQuantity, TextField txtQuantity,
                    Label lblTotal, int index) {
            this.lblPrice = lblPrice;
            this.txtPrice = txtPrice;
            this.lblQuantity = lblQuantity;
            this.txtQuantity = txtQuantity;
            this.lblTotal = lblTotal;
            this.index = index;
        }
    }
}
