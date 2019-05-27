package com.xjtu.power.agent.TransactionMemberAgent;

import com.xjtu.power.entity.Seller;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author: Jay
 * @Date: Created in 9:26 2019/3/26
 * @Modified By:
 */
@Setter
@Getter
public class SellerBidAgent extends AbstractBidAgent{

    private Seller sellerInfo;

    private String name;

    private double quantity;

    private double price;

    private double maxQuantity;

    private double finalQuantity;

    private double finalPrice;

    private int stateCnt;
    private int actionCnt;

    double a = 0.03, b = 20, c = 0.0;

    public SellerBidAgent(int stateCount, int actionCount) {
        super(stateCount, actionCount);
    }

    public SellerBidAgent(int stateCount, int actionCount, Seller sellerInfo,  String name, double quantity, double price, double maxQuantity,double a,double b,double c) {
        super(stateCount, actionCount);
        this.sellerInfo = sellerInfo;
/*        name = sellerInfo.getName();
        quantity = sellerInfo.getQuantity();
        price = sellerInfo.getPrice();
        maxQuantity = sellerInfo.getMaxQuantity();
        finalQuantity = sellerInfo.getFinalQuantity();
        finalPrice = sellerInfo.getFinalPrice();*/
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.maxQuantity = maxQuantity;
        this.stateCnt = stateCount;
        this.actionCnt = actionCount;
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public SellerBidAgent(int stateCount, int actionCount, String name, double quantity, double price,double a,double b,double c) {
        super(stateCount, actionCount);
        this.sellerInfo = sellerInfo;
/*        name = sellerInfo.getName();
        quantity = sellerInfo.getQuantity();
        price = sellerInfo.getPrice();
        maxQuantity = sellerInfo.getMaxQuantity();
        finalQuantity = sellerInfo.getFinalQuantity();
        finalPrice = sellerInfo.getFinalPrice();*/
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.stateCnt = stateCount;
        this.actionCnt = actionCount;
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public SellerBidAgent(int stateCount, int actionCount,String name, double quantity, double price){
        super(stateCount, actionCount);
        this.stateCnt = stateCount;
        this.actionCnt = actionCount;
        this.name = name;
        this.quantity = quantity;
        this.price = price;

    }


    @Override
    public double cost() {
        double ans = 0.00;
        ans = a*finalQuantity*finalQuantity + b*finalQuantity + c;
        return ans;
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
    public double revenue() {
        double revenue = finalPrice * finalQuantity;
        return revenue;
    }

    /**
     * 这里需要将actionID转换为对应的可变系数。
     * @return
     */
    @Override
    public void bidPrice(int actionId) {
        //对actionID进行变换,使用actionCnt
//        todo
        double coff = 1.00;
        coff = 0.7 +  (1.5-0.7)*actionId/actionCnt;
        double ans = 0.0;
        ans = 2*a*quantity + b;
        price = ans*coff;
    }

    /**
     * Qtable是一个横坐标是状态（出清价格），纵坐标是动作（选择策略的变化系数）的表
     * 如何去将坐标起始量与实际的对应起来？
     *
     * 最后agent是这样进行更新的：
     *      agent.update(actionId, newStateId, reward);
     * 其中的actionId，newStateId都是从0开始的。
     *
     * 而actionID是这样进行更新的（由选择策略决定）：
     *      int actionId = agent.selectAction().getIndex();
     * stateId是这样维护的:
     *      int newStateId = buyBidAgent.update(actionId);
     *      在本例中即为出清价格-最低出清价格，得到偏移量。
     * 在获得两者以后，需要计算reward：
     *      double reward = buyBidAgent.reward();
     * @return
     */
    @Override
    public int stateTransform() {
//        stateCnt将出清价格转换为状态空间的行数
//        todo
//        划分为10-50的区间，每个间隔是2,20个状态
//        需要先确定在哪个区间，比如说41.25，
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
    public int update(int actionId) {
        bidPrice(actionId);
        return stateTransform();
    }



    @Override
    public void writeBack() {
        finalQuantity = 0.0;
//        quantity = finalQuantity;
    }

    @Override
    public double commit() {
        return 0;
    }

}
