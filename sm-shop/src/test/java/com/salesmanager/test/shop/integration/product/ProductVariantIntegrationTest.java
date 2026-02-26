package com.salesmanager.test.shop.integration.product;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.salesmanager.core.business.constants.Constants;
import com.salesmanager.shop.application.ShopApplication;
import com.salesmanager.shop.model.catalog.product.ProductPriceRequest;
import com.salesmanager.shop.model.catalog.product.ReadableProduct;
import com.salesmanager.shop.model.catalog.product.ReadableProductPrice;
import com.salesmanager.test.shop.common.ServicesTestSupport;

@SpringBootTest(classes = ShopApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class ProductVariantIntegrationTest extends ServicesTestSupport {

    @Test
    public void testGetProductPrice_whenUncommented_returnsPrice() {
        ReadableProduct product = sampleProduct("variantTest");
        assertNotNull(product);

        ProductPriceRequest priceRequest = new ProductPriceRequest();
        // Just empty options for now, should still return base price
        
        final HttpEntity<ProductPriceRequest> entity = new HttpEntity<>(priceRequest, getHeader());
        final ResponseEntity<ReadableProductPrice> response = testRestTemplate.postForEntity(
                "/api/v1/product/" + product.getId() + "/price?store=" + Constants.DEFAULT_STORE, 
                entity, ReadableProductPrice.class);

        assertThat("Price API should return 200 OK after being uncommented", 
                response.getStatusCode(), is(HttpStatus.OK));
        assertNotNull(response.getBody());
    }

    @Test
    public void testGetProductByCode_includesVariantsList() {
        ReadableProduct product = sampleProduct("variantListTest");
        assertNotNull(product);

        final HttpEntity<String> httpEntity = new HttpEntity<>(getHeader());
        ResponseEntity<ReadableProduct> response = testRestTemplate.exchange(
                "/api/v2/product/" + product.getSku() + "?store=" + Constants.DEFAULT_STORE, 
                HttpMethod.GET, httpEntity, ReadableProduct.class);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertNotNull("Variants list should be present (even if empty)", response.getBody().getVariants());
    }
}
