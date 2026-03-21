package com.example.demo.controller;

import com.example.demo.model.Account;
import com.example.demo.model.Cart;
import com.example.demo.model.Product;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ProductService;
import com.example.demo.service.AccountService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Optional; 
import java.util.List;


@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final AccountService accountService;

    // Display a list of all products
    @GetMapping
    public String showProductList(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "/products/product-list";
    }

    @GetMapping("/detail/{id}")
    public String showProduct() {
        return "forward:/products/product-detail.html";
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<?> getproduct(@PathVariable Long id) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        return ResponseEntity.ok(product);
    }

    @GetMapping("/category={id}")
    public ResponseEntity<?> getproductsByCategory(@PathVariable Long id) {
        List<Product> products = productService.getProductsByCategory(id);
        return ResponseEntity.ok(products);
    }


    // For adding a new product
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "/products/add-product";
    }

    // Process the form for adding a new product
    @PostMapping("/add")
        public String addProduct(@Valid Product product,
                                BindingResult result,
                                @RequestParam("image") MultipartFile image,
                                Model model) {

            if (result.hasErrors()) {
                model.addAttribute("categories", categoryService.getAllCategories());
                return "/products/add-product";
            }

            if (!image.isEmpty()) {
                try {
                    String uploadDir = "src/main/resources/static/productsimg/";
                    String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();

                    Path path = Paths.get(uploadDir, fileName);
                    Files.createDirectories(path.getParent());
                    Files.write(path, image.getBytes());

                    // 🔑 THIS is what saves image_string
                    product.setImage_string("/productsimg/" + fileName);

                } catch (Exception e) {
                    throw new RuntimeException("Image upload failed", e);
                }
            }

            productService.addProduct(product);
            return "redirect:/products";
        }


    // For editing a product
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "/products/update-product";
    }

    // Process the form for updating a product
    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable Long id,
                                @Valid Product product,
                                BindingResult result,
                                @RequestParam("image") MultipartFile image,
                                Model model) {

        if (result.hasErrors()) {
            product.setId(id);
            model.addAttribute("categories", categoryService.getAllCategories());
            return "/products/update-product";
        }

        Product existingProduct = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));

        // ✅ Handle image upload
        if (!image.isEmpty()) {
            try {
                String uploadDir = "src/main/resources/static/productsimg/";
                String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();

                Path path = Paths.get(uploadDir, fileName);
                Files.createDirectories(path.getParent());
                Files.write(path, image.getBytes());

                product.setImage_string("/productsimg/" + fileName);

            } catch (Exception e) {
                throw new RuntimeException("Image upload failed", e);
            }
        } else {
            // ✅ Keep old image if no new one uploaded
            product.setImage_string(existingProduct.getImage_string());
        }

        product.setId(id);
        productService.updateProduct(product);

        return "redirect:/products";
    }


    // Handle request to delete a product
    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProductById(id);
        return "redirect:/products";
    }

    @GetMapping("/cart")
    public String gotocart() {
        return "forward:/products/cart.html";
    }

    @PostMapping("/add-to-cart/{id}")
    public ResponseEntity<?> addtocart(HttpSession session, @PathVariable Long id) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        Long accountid = (Long) session.getAttribute("userId");
        Account account = accountService.getAccountById(accountid)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Account Id:" + accountid));
        productService.addToCart(id, accountid);
        return ResponseEntity.ok("Product added to cart");
    }

    @GetMapping("add-to-cart/{id}")
    public ResponseEntity<?> showAddToCartPage(@PathVariable Long id) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        return ResponseEntity.ok(product);
    }

    @GetMapping("/pullcart")
    public ResponseEntity<?> addtocart(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        Long accountid = (Long) session.getAttribute("userId");
        List<Cart> cartItems = productService.getCartByAccountId(accountid)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Account Id:" + accountid));
        return ResponseEntity.ok(cartItems);
    }

    @PostMapping("/placeorder")
    public ResponseEntity<?> placeorder(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        Long accountid = (Long) session.getAttribute("userId");
        productService.placeorder(accountid);
        return ResponseEntity.ok("Order placed successfully");
    }
}