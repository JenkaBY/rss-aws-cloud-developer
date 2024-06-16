package by.jenka.rss.lambda.model;


public class ProductResponse {
    private Product product;

    public ProductResponse(Product product) {
        this.product = product;
    }


    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
