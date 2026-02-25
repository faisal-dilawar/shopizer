package com.salesmanager.test.business.utils;

import com.salesmanager.core.business.utils.ProductPriceUtils;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.core.model.reference.country.Country;
import com.salesmanager.core.model.reference.language.Language;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ProductPriceUtils} currency symbol formatting.
 * Follows the same Mockito pattern as DataUtilsTest — no Spring context needed.
 */
public class ProductPriceUtilsTest {

    private final ProductPriceUtils priceUtils = new ProductPriceUtils();

    // ── helpers ──────────────────────────────────────────────────────────────

    private MerchantStore buildStore(Currency shopCurrency, boolean nationalFormat) {
        Language language = mock(Language.class);
        when(language.getCode()).thenReturn("en");

        Country country = mock(Country.class);
        when(country.getIsoCode()).thenReturn("IN");

        MerchantStore store = mock(MerchantStore.class);
        when(store.getCurrency()).thenReturn(shopCurrency);
        when(store.isCurrencyFormatNational()).thenReturn(nationalFormat);
        when(store.getDefaultLanguage()).thenReturn(language);
        when(store.getCountry()).thenReturn(country);
        when(store.getCode()).thenReturn("DEFAULT");
        return store;
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    @Test
    public void getStoreFormatedAmount_noOverride_containsJavaDefaultSymbol() throws Exception {
        Currency shopCurrency = new Currency();
        shopCurrency.setCurrency(java.util.Currency.getInstance("INR"));
        // No symbolOverride set

        MerchantStore store = buildStore(shopCurrency, true);

        String result = priceUtils.getStoreFormatedAmountWithCurrency(store, new BigDecimal("1500.00"));

        // In this environment, Java's default INR symbol is "₹" — must appear; override "Rs." must NOT
        assertTrue("Expected Java default symbol ₹ in: " + result, result.contains("₹"));
        assertFalse("Override Rs. must not appear when not set: " + result, result.contains("Rs."));
    }

    @Test
    public void getStoreFormatedAmount_withOverride_containsOverrideSymbol() throws Exception {
        Currency shopCurrency = new Currency();
        shopCurrency.setCurrency(java.util.Currency.getInstance("INR"));
        shopCurrency.setSymbolOverride("Rs.");

        MerchantStore store = buildStore(shopCurrency, true);

        String result = priceUtils.getStoreFormatedAmountWithCurrency(store, new BigDecimal("1500.00"));

        assertTrue("Expected override symbol Rs. in: " + result, result.contains("Rs."));
        assertFalse("Java symbol ₹ must not appear when override is set: " + result, result.contains("₹"));
    }

    @Test
    public void getStoreFormatedAmount_blankOverride_containsJavaDefaultSymbol() throws Exception {
        Currency shopCurrency = new Currency();
        shopCurrency.setCurrency(java.util.Currency.getInstance("INR"));
        shopCurrency.setSymbolOverride("   "); // blank — must behave like no override

        MerchantStore store = buildStore(shopCurrency, true);

        String result = priceUtils.getStoreFormatedAmountWithCurrency(store, new BigDecimal("1500.00"));

        assertTrue("Expected Java default symbol ₹ in: " + result, result.contains("₹"));
    }

    @Test
    public void getFormatedAmountWithCurrency_withOverride_containsOverrideSymbol() throws Exception {
        Currency shopCurrency = new Currency();
        shopCurrency.setCurrency(java.util.Currency.getInstance("INR"));
        shopCurrency.setSymbolOverride("Rs.");

        java.util.Locale locale = new java.util.Locale("en", "IN");
        String result = priceUtils.getFormatedAmountWithCurrency(locale, shopCurrency, new BigDecimal("1500.00"));

        assertTrue("Expected override symbol Rs. in: " + result, result.contains("Rs."));
        assertFalse("Java symbol ₹ must not appear when override is set: " + result, result.contains("₹"));
    }

    @Test
    public void getFormatedAmountWithCurrency_noOverride_containsJavaDefaultSymbol() throws Exception {
        Currency shopCurrency = new Currency();
        shopCurrency.setCurrency(java.util.Currency.getInstance("INR"));
        // No symbolOverride

        java.util.Locale locale = new java.util.Locale("en", "IN");
        String result = priceUtils.getFormatedAmountWithCurrency(locale, shopCurrency, new BigDecimal("1500.00"));

        assertTrue("Expected Java default symbol ₹ in: " + result, result.contains("₹"));
        assertFalse("Override Rs. must not appear when not set: " + result, result.contains("Rs."));
    }
}
