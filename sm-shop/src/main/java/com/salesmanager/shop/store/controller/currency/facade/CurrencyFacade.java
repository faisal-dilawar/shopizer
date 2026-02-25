package com.salesmanager.shop.store.controller.currency.facade;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.model.reference.currency.Currency;
import java.util.List;

public interface CurrencyFacade {

  List<Currency> getList();

  /**
   * Updates the CURRENCY_SYMBOL_OVERRIDE for the given currency code.
   * Pass null or empty string to clear the override and fall back to the Java default.
   */
  void updateSymbolOverride(String code, String symbolOverride) throws ServiceException;
}
