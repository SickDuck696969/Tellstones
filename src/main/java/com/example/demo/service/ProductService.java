package com.example.demo.service;

import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import com.example.demo.model.Cart;
import com.example.demo.model.Order;
import com.example.demo.model.OrderDetail;
import com.example.demo.repository.OrderDetailRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.AccountRepository;
import com.example.demo.model.Account;
import lombok.RequiredArgsConstructor; 
import org.springframework.stereotype.Service; 
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.constraints.NotNull; 
import java.util.List; 
import java.util.Optional; 
import java.util.Map;
import java.util.HashMap;
 
@Service 
@RequiredArgsConstructor 
@Transactional 
public class ProductService { 
    private final ProductRepository productRepository; 
    private final CartRepository cartRepository;
    private final AccountRepository accountRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    // Retrieve all products from the database 
    public List<Product> getAllProducts() { 
        return productRepository.findAll(); 
    } 

    public List<Product> getProductsByCategory(long category_id) {
        return productRepository.findBycategory_Id(category_id);
    }
 
    // Retrieve a product by its id 
    public Optional<Product> getProductById(Long id) { 
        return productRepository.findById(id); 
    } 
 
    // Add a new product to the database 
    public Product addProduct(Product product) { 
        return productRepository.save(product); 
    } 
 
    // Update an existing product 
    public Product updateProduct(@NotNull Product product) {
        Product existingProduct = productRepository.findById(product.getId())
            .orElseThrow(() -> new IllegalStateException("Product with ID " + product.getId() + " does not exist."));

        existingProduct.setName(product.getName());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setImage_string(product.getImage_string());

        return productRepository.save(existingProduct);
    }

 
    // Delete a product by its id 
    public void deleteProductById(Long id) { 
        if (!productRepository.existsById(id)) {
            throw new IllegalStateException("Product with ID " + id + " does not exist.");
        }
        productRepository.deleteById(id);
    }

    public void addToCart(Long productId, Long accountId) {
        Cart cartItem = new Cart();
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalStateException("Product with ID " + productId + " does not exist."));
        cartItem.setProduct(product);
        cartItem.setAccount(accountRepository.findById(accountId)
            .orElseThrow(() -> new IllegalStateException("Account with ID " + accountId + " does not exist.")));
        cartItem.setCategory(product.getCategory());
        cartRepository.save(cartItem);
    }

    public Optional<List<Cart>> getCartByAccountId(Long accountId) {
        return Optional.of(cartRepository.findByaccount_id(accountId));
    }

    public void placeorder(Long accountId) {
        Order order = new Order();
        Account acc = accountRepository.findById(accountId)
            .orElseThrow(() -> new IllegalStateException("Account with ID " + accountId + " does not exist."));
        order.setAccount(acc);
        orderRepository.save(order);
        List<Cart> cartItems = cartRepository.findByaccount_id(accountId);
        Map<Long, Integer> productQuantityMap = new HashMap<Long, Integer>();
        cartItems.forEach(cart -> {
            if(productQuantityMap.containsKey(cart.getProduct().getId())){
                productQuantityMap.put(cart.getProduct().getId(), productQuantityMap.get(cart.getProduct().getId()) + 1);
            } else {
                productQuantityMap.put(cart.getProduct().getId(), 1);
            }
        });
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrder(order);
        int a = 0;
        for (Map.Entry<Long, Integer> entry : productQuantityMap.entrySet()) {
            Long key = entry.getKey();
            Integer value = entry.getValue();
            
            Product k = productRepository.findById(key)
                .orElseThrow(() -> new IllegalStateException("Product with ID " + key + " does not exist."));
            orderDetail.setProduct(k);
            orderDetail.setQuantity(value);
            a += k.getPrice();
        }
        if (a >= 2000000 && productQuantityMap.size() >= 2){
            orderDetail.setShipping(0);
        }
        else {
            orderDetail.setShipping(30000);
        }
        acc.setCredit(acc.getCredit() + 10000);
        orderDetailRepository.save(orderDetail);
    }
} 