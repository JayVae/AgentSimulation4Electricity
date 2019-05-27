package com.xjtu.power.agent.TransactionMemberAgent;

import com.xjtu.power.entity.Buyer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author: Jay
 * @Date: Created in 10:45 2019/3/26
 * @Modified By:
 */
@Getter
@Setter
public class BuyerBidAgent extends AbstractBidAgent{

    private Buyer buyerInfo;

    private String name;

    private double quantity;

    private double price;

    private double maxQuantity;

    private double finalQuantity;

    private double finalPrice;

    private int stateCnt;
    private int actionCnt;

    double e = 60, f = 0.05;

    public BuyerBidAgent(int stateCount, int actionCount) {
        super(stateCount, actionCount);
    }

    public BuyerBidAgent(int stateCount, int actionCount, Buyer buyerInfo, String name, double quantity, double price, double maxQuantity,double e,double f) {
        super(stateCount, actionCount);
        this.buyerInfo = buyerInfo;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.maxQuantity = maxQuantity;
        this.stateCnt = stateCount;
        this.actionCnt = actionCount;
        this.e = e;
        this.f = f;
    }

    public BuyerBidAgent(int stateCount, int actionCount, String name, double quantity, double price,double e,double f) {
        super(stateCount, actionCount);
        this.buyerInfo = buyerInfo;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.stateCnt = stateCount;
        this.actionCnt = actionCount;
        this.e = e;
        this.f = f;
    }

    public BuyerBidAgent(int stateCount, int actionCount,String name, double quantity, double price){
        super(stateCount, actionCount);
        this.name = name;
        this.quantity = quantity;
        this.price = price;

    }

    public BuyerBidAgent(int stateCount, int actionCount, Buyer buyerInfo) {
        super(stateCount, actionCount);
        this.buyerInfo = buyerInfo;
        name = buyerInfo.getName();
        quantity = buyerInfo.getQuantity();
        price = buyerInfo.getPrice();
        maxQuantity = buyerInfo.getMaxQuantity();
        finalQuantity = buyerInfo.getFinalQuantity();
        finalPrice = buyerInfo.getFinalPrice();
        stateCnt = stateCount;
        actionCnt = actionCount;
    }

    @Override
    public double cost() {
        double cost = 0.00;
        cost = finalPrice * finalQuantity;
        return cost;
    }

    @Override
    public double revenue() {
        double ans = 0.00;
        ans = e*finalQuantity - f*finalQuantity*finalQuantity ;
        return ans;
    }

    @Override
    public void bidPrice(int actionId) {
        //对actionID进行变换,使用actionCnt
//        todo
        double coff = 1.00;
        coff = 0.8 +  (1.8-0.8)*actionId/actionCnt;
        double ans = 0.0;
        ans = e - 2*f*quantity ;
        price = ans*coff;
    }

    @Override
    public double reward() {
        double ans = 0.0;
        double cost = cost();
        double revenue = revenue();
        ans = revenue - cost;
        return ans;
    }

    @Override
    public int stateTransform() {
        int stateId = (int)((finalPrice-10)/2);
        if (stateId<0){
            stateId = 0;
        }
        if (stateId>=20){
            stateId = 19;
        }
        return stateId;
    }

    @Override
    public double commit() {
        return 0;
    }

    @Override
    public int update(int actionId) {
        bidPrice(actionId);
        return stateTransform();
    }

    @Override
    public void writeBack() {
        finalQuantity = 0.0;
//        quantity应该是不变的，作为初始报量
//        quantity = finalQuantity;
    }
}
