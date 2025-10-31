package exe.SonMaiHeritage.service;

import exe.SonMaiHeritage.entity.Basket;
import exe.SonMaiHeritage.model.BasketResponse;

import java.util.List;

public interface BasketService {
    List<BasketResponse> getAllBaskets();
    BasketResponse getBasketById(String basketId);
    void deleteBasketById(String basketId);
    BasketResponse createBasket(Basket basket);
}
