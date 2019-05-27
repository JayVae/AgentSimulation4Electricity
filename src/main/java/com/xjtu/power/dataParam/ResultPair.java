package com.xjtu.power.dataParam;


import com.xjtu.power.agent.TransactionMemberAgent.BuyerBidAgent;
import com.xjtu.power.agent.TransactionMemberAgent.SellerBidAgent;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: Jay
 * @Date: Created in 19:55 2018/12/26
 * @Modified By:
 */

@Getter
public class ResultPair {
    /**
     * 在diffPair中称为diff
     */
    private double quantity;


    private BuyerBidAgent buyer;

    private SellerBidAgent seller;

    public ResultPair(double quantity, BuyerBidAgent buyer, SellerBidAgent seller) {
        this.quantity = quantity;
        this.buyer = buyer;
        this.seller = seller;
    }

    public void printList(){
        System.out.println("*****"+seller.getName()+"*****"+buyer.getName()+"*****"+quantity+"*****");
    }
}
