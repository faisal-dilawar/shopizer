package com.salesmanager.test.references;

import com.salesmanager.core.model.reference.currency.Currency;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link Currency#getSymbol()}.
 * No Spring context needed — Currency is a plain entity.
 */
public class CurrencyTest {

    @Test
    public void getSymbol_whenOverrideNotSet_returnsJavaDefault() {
        Currency currency = new Currency();
        currency.setCurrency(java.util.Currency.getInstance("INR"));
        // In this environment, Java's default symbol for INR is "₹"
        assertEquals("₹", currency.getSymbol());
    }

    @Test
    public void getSymbol_whenOverrideSet_returnsOverride() {
        Currency currency = new Currency();
        currency.setCurrency(java.util.Currency.getInstance("INR"));
        currency.setSymbolOverride("Rs.");
        assertEquals("Rs.", currency.getSymbol());
    }

    @Test
    public void getSymbol_whenOverrideIsBlank_returnsJavaDefault() {
        // Blank/whitespace override must be treated the same as no override
        Currency currency = new Currency();
        currency.setCurrency(java.util.Currency.getInstance("INR"));
        currency.setSymbolOverride("   ");
        assertEquals("₹", currency.getSymbol());
    }

    @Test
    public void getSymbol_whenOverrideCleared_returnsJavaDefault() {
        // Setting override then clearing it (null) must fall back to Java default
        Currency currency = new Currency();
        currency.setCurrency(java.util.Currency.getInstance("INR"));
        currency.setSymbolOverride("Rs.");
        assertEquals("Rs.", currency.getSymbol());

        currency.setSymbolOverride(null);
        assertEquals("₹", currency.getSymbol());
    }

    @Test
    public void getSymbol_differentCurrency_overrideApplied() {
        // Override works for other currencies, not just INR
        Currency currency = new Currency();
        currency.setCurrency(java.util.Currency.getInstance("USD"));
        currency.setSymbolOverride("US$");
        assertEquals("US$", currency.getSymbol());
    }
}
