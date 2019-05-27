package com.xjtu.power.agent.TransactionMemberAgent;

import com.xjtu.power.dataParam.ClearingResult;
import com.xjtu.power.dataParam.ResultPair;
import com.xjtu.power.entity.Seller;
import com.xjtu.power.tool.ClearingTool;

import java.util.*;

/**
 * @Author: Jay
 * @Date: Created in 15:04 2019/4/2
 * @Modified By:
 */
public class PXCAgent {


    public ClearingResult Clearing(List<BuyerBidAgent> buyerList, List<SellerBidAgent> sellerList,Double maxQuantity){
        //        1.数据有效筛选
        List<BuyerBidAgent> buyers = filterBuyer(buyerList);
        List<SellerBidAgent> sellers = filterSeller(sellerList);
//        2.交易排序
        Collections.sort(sellers, new ClearingTool.PriceIncComparator()); // 根据价格排序
        Collections.sort(buyers, new ClearingTool.PriceDecComparator()); // 根据价格排序
//        3.交易竞价
//        ClearingResult clearingResult = bid(buyers,sellers,maxQuantity);
        ClearingResult clearingResult = bid2(buyers,sellers,maxQuantity);
        return clearingResult;
    }

    private ClearingResult bid2(List<BuyerBidAgent> buyers, List<SellerBidAgent> sellers, Double maxQuantity) {
        ClearingResult cr = new ClearingResult();
        ArrayList<ResultPair> resultPairs = new ArrayList<ResultPair>();
        Double clearPrice = 0.0;
        double accumQuantity = 0.0;

        List<ResultPair> diffPairs = new ArrayList<>();
        for (int i = 0; i < buyers.size(); i++) {
            for (int j = 0; j < sellers.size(); j++) {
                double diff = buyers.get(i).getPrice() - sellers.get(j).getPrice();
                if (diff>=0){
                    ResultPair diffPair = new ResultPair(diff,buyers.get(i),sellers.get(j));
                    diffPairs.add(diffPair);
                }
            }
        }
        Collections.sort(diffPairs, new ClearingTool.DiffDecComparator());
//
        Set<SellerBidAgent> blackSellerSet = new HashSet<>();
        Set<BuyerBidAgent> blackBuyerSet = new HashSet<>();

//        每次都要向后进行探索 ，直到没有与当前相同的才开始对当前的进行分配。
        List<ResultPair> tmpDiff = new ArrayList<>();
        for (int j = 0; j < diffPairs.size(); j++) {

                ResultPair thisDiff = diffPairs.get(j);
                ResultPair nextDiff ;
                if (j == diffPairs.size()-1){
    //                直接进行分配，处理逻辑和下面是一样的，按照tmpDiff是不是空的进行处理
                    nextDiff = new ResultPair(-100000.00,thisDiff.getBuyer(),thisDiff.getSeller());
                }else {
                    nextDiff = diffPairs.get(j+1);
                }
                if (thisDiff.getQuantity()==nextDiff.getQuantity()){
//                    这里可能会多添加一次
                    BuyerBidAgent buyer = thisDiff.getBuyer();
                    SellerBidAgent seller = thisDiff.getSeller();
                    if (blackBuyerSet.contains(buyer) || blackSellerSet.contains(seller)){
                        continue;
                    }
                    tmpDiff.add(thisDiff);
//                        tmpDiff.add(nextDiff);

                }else {
//                    进行分配并清空tmpDiff
                    if (tmpDiff.isEmpty()){
//                        对当前的进行处理
                        BuyerBidAgent buyer = thisDiff.getBuyer();
                        SellerBidAgent seller = thisDiff.getSeller();
                        if (blackBuyerSet.contains(buyer) || blackSellerSet.contains(seller)){
                            continue;
                        }
                        if (accumQuantity >= maxQuantity){
                            System.out.println("总共成交了"+accumQuantity);
                            break;
                        }
                        Double quantityNow = 0.0;
                        quantityNow = Math.min(Math.min(buyer.getQuantity()-buyer.getFinalQuantity(),seller.getQuantity()-seller.getFinalQuantity()),maxQuantity-accumQuantity);
                        buyer.setFinalQuantity(buyer.getFinalQuantity()+quantityNow);
                        seller.setFinalQuantity(seller.getFinalQuantity()+quantityNow);
                        ResultPair resultPair = new ResultPair(quantityNow,buyer,seller);
                        accumQuantity += quantityNow;
                        resultPairs.add(resultPair);
                        clearPrice = (buyer.getPrice()+seller.getPrice())/2;
                        if (buyer.getFinalQuantity() == buyer.getQuantity() || seller.getFinalQuantity() == seller.getQuantity()){
                            if (buyer.getFinalQuantity() == buyer.getQuantity()){
                                blackBuyerSet.add(buyer);
                            }
                            if (seller.getFinalQuantity() == seller.getQuantity()){
                                blackSellerSet.add(seller);
                            }
                        }
                    }else{
//                        对之前相同以及这次的"一起"进行处理，并清空
                        BuyerBidAgent buyer = thisDiff.getBuyer();
                        SellerBidAgent seller = thisDiff.getSeller();
//                        这里直接跳回是不对的
                        if (blackBuyerSet.contains(buyer) || blackSellerSet.contains(seller)){

                        }else{
                            tmpDiff.add(thisDiff);
                        }
                        if (accumQuantity >= maxQuantity){
                            System.out.println("总共成交了"+accumQuantity);
                            break;
                        }
                        Double quantityTmp = 0.0;
                        Double quantityTmpBuyer = 0.0;
                        Double quantityTmpSeller = 0.0;
                        List<BuyerBidAgent> buyersThis = new ArrayList<>();
                        List<SellerBidAgent> sellersThis = new ArrayList<>();
                        for (int k = 0; k < tmpDiff.size(); k++) {
                            BuyerBidAgent buyerThis = tmpDiff.get(k).getBuyer();
                            SellerBidAgent sellerThis = tmpDiff.get(k).getSeller();
                            if (buyersThis.contains(buyerThis)){

                            }else {
                                buyersThis.add(buyerThis);
                                quantityTmpBuyer += buyerThis.getQuantity()-buyerThis.getFinalQuantity();
                            }
                            if (sellersThis.contains(sellerThis)){

                            }else{
                                sellersThis.add(sellerThis);
                                quantityTmpSeller += sellerThis.getQuantity()-sellerThis.getFinalQuantity();
                            }
                        }
                        double[] ratioBuyer = new double[buyersThis.size()];
                        double[] ratioSeller = new double[sellersThis.size()];
                        for (int k = 0; k < buyersThis.size(); k++) {
                            BuyerBidAgent buyerThis = buyersThis.get(k);
                            ratioBuyer[k] = (buyerThis.getQuantity()-buyerThis.getFinalQuantity())/quantityTmpBuyer;
                        }
                        for (int k = 0; k < sellersThis.size(); k++) {
                            SellerBidAgent sellerThis = sellersThis.get(k);
                            ratioSeller[k] = (sellerThis.getQuantity()-sellerThis.getFinalQuantity())/quantityTmpSeller;
                        }
                        quantityTmp = Math.min(quantityTmpBuyer,quantityTmpSeller);
                        double accumQuantityBefore = accumQuantity;
                        if (quantityTmpBuyer > quantityTmpSeller){

                            for (int k = 0; k < sellersThis.size(); k++) {

                                SellerBidAgent sellerThis = sellersThis.get(k);
                                double sellerQuantityThis = sellerThis.getQuantity()-sellerThis.getFinalQuantity();
                                if ((quantityTmp + accumQuantityBefore)>= maxQuantity){
                                    sellerQuantityThis = (maxQuantity-accumQuantityBefore)*ratioSeller[k];
                                }
                                for (int l = 0; l < buyersThis.size(); l++) {
                                    BuyerBidAgent buyerThis = buyersThis.get(l);
                                    double quantityThisNow = ratioBuyer[l]*sellerQuantityThis;
                                    buyerThis.setFinalQuantity(buyerThis.getFinalQuantity()+quantityThisNow);
                                    sellerThis.setFinalQuantity(sellerThis.getFinalQuantity()+quantityThisNow);
                                    ResultPair resultPair = new ResultPair(quantityThisNow,buyerThis,sellerThis);
                                    accumQuantity += quantityThisNow;
                                    resultPairs.add(resultPair);
                                    clearPrice = (buyerThis.getPrice()+sellerThis.getPrice())/2;
                                    if (buyerThis.getFinalQuantity() == buyerThis.getQuantity() || sellerThis.getFinalQuantity() == sellerThis.getQuantity()){
                                        if (buyerThis.getFinalQuantity() == buyerThis.getQuantity()){
                                            blackBuyerSet.add(buyerThis);
                                        }
                                        if (sellerThis.getFinalQuantity() == sellerThis.getQuantity()){
                                            blackSellerSet.add(sellerThis);
                                        }
                                    }
                                }
                            }
                        }else {

                            for (int k = 0; k < buyersThis.size(); k++) {
                                BuyerBidAgent buyerThis = buyersThis.get(k);
                                double buyerQuantityThis = buyerThis.getQuantity()-buyerThis.getFinalQuantity();
                                if ((quantityTmp + accumQuantityBefore)>= maxQuantity){
                                    buyerQuantityThis = (maxQuantity-accumQuantityBefore)*ratioBuyer[k];
                                }
                                for (int l = 0; l < sellersThis.size(); l++) {
                                    SellerBidAgent sellerThis = sellersThis.get(l);
                                    double quantityThisNow = ratioSeller[l]*buyerQuantityThis;
                                    buyerThis.setFinalQuantity(buyerThis.getFinalQuantity()+quantityThisNow);
                                    sellerThis.setFinalQuantity(sellerThis.getFinalQuantity()+quantityThisNow);
                                    ResultPair resultPair = new ResultPair(quantityThisNow,buyerThis,sellerThis);
                                    accumQuantity += quantityThisNow;
                                    resultPairs.add(resultPair);
                                    clearPrice = (buyerThis.getPrice()+sellerThis.getPrice())/2;
                                    if (buyerThis.getFinalQuantity() == buyerThis.getQuantity() || sellerThis.getFinalQuantity() == sellerThis.getQuantity()){
                                        if (buyerThis.getFinalQuantity() == buyerThis.getQuantity()){
                                            blackBuyerSet.add(buyerThis);
                                        }
                                        if (sellerThis.getFinalQuantity() == sellerThis.getQuantity()){
                                            blackSellerSet.add(sellerThis);
                                        }
                                    }
                                }
                            }
                        }
                        tmpDiff.clear();
/*                        for (int k = 0; k < tmpDiff.size(); k++) {
//                            进行分配
                            BuyerBidAgent buyerThis = tmpDiff.get(k).getBuyer();
                            SellerBidAgent sellerThis = tmpDiff.get(k).getSeller();
                            Double quantityThis = 0.0;
//                           这里并不是其实际值，如何处理max
                            quantityThis = Math.min(Math.min(buyerThis.getQuantity()-buyerThis.getFinalQuantity(),sellerThis.getQuantity()-sellerThis.getFinalQuantity()),maxQuantity-accumQuantity);
                            quantityTmp += quantityThis;
                            quantityTmpBuyer += buyerThis.getQuantity()-buyerThis.getFinalQuantity();
                            quantityTmpSeller += sellerThis.getQuantity()-sellerThis.getFinalQuantity();
                            if ((accumQuantity+quantityTmp)>=maxQuantity){
                                quantityTmp = maxQuantity - accumQuantity;
                            }
                        }*/
         /*               for (int k = 0; k < tmpDiff.size(); k++) {
                            BuyerBidAgent buyerThis = tmpDiff.get(k).getBuyer();
                            SellerBidAgent sellerThis = tmpDiff.get(k).getSeller();
                            ratioBuyer[k] = (buyerThis.getQuantity()-buyerThis.getFinalQuantity())/quantityTmpBuyer;
                            ratioSeller[k] = (sellerThis.getQuantity()-sellerThis.getFinalQuantity())/quantityTmpSeller;
                        }*/
                        /*for (int k = 0; k < tmpDiff.size(); k++) {
                            BuyerBidAgent buyerThis = tmpDiff.get(k).getBuyer();
                            SellerBidAgent sellerThis = tmpDiff.get(k).getSeller();
                            buyerThis.setFinalQuantity(buyerThis.getFinalQuantity()+ratioBuyer[k]*quantityTmp);
                            sellerThis.setFinalQuantity(sellerThis.getFinalQuantity()+ratioSeller[k]*quantityTmp);
                            ResultPair resultPair = new ResultPair(quantityNow,buyerThis,sellerThis);
                            accumQuantity += quantityNow;
                            resultPairs.add(resultPair);
                            clearPrice = (buyerThis.getPrice()+sellerThis.getPrice())/2;
                            if (buyerThis.getFinalQuantity() == buyerThis.getQuantity() || sellerThis.getFinalQuantity() == sellerThis.getQuantity()){
                                if (buyerThis.getFinalQuantity() == buyerThis.getQuantity()){
                                    blackBuyerSet.add(buyerThis);
                                }
                                if (sellerThis.getFinalQuantity() == sellerThis.getQuantity()){
                                    blackSellerSet.add(sellerThis);
                                }
                            }
                        }*/
                    }
                }

        }
        cr.setClearPrice(clearPrice);
        for (SellerBidAgent seller : sellers) {
            seller.setFinalPrice(clearPrice);
        }
        for (BuyerBidAgent buyer : buyers) {
            buyer.setFinalPrice(clearPrice);
        }
        cr.setResultPairList(resultPairs);
        return cr;
    }

    private ClearingResult bid(List<BuyerBidAgent> buyers, List<SellerBidAgent> sellers, Double maxQuantity){

        ClearingResult cr = new ClearingResult();
        ArrayList<ResultPair> resultPairs = new ArrayList<ResultPair>();
        Double clearPrice = 0.0;
        int i = 0, j = 0;
        double accumQuantity = 0.0;
        while(i!=buyers.size() && j!=sellers.size()){
            BuyerBidAgent buyer = buyers.get(i);
            SellerBidAgent seller = sellers.get(j);
            Double quantityNow = 0.0;
            if(buyer.getPrice()<seller.getPrice()){
                break;
            }
            if (accumQuantity >= maxQuantity){
                System.out.println("总共成交了"+accumQuantity);
                break;
            }
            quantityNow = Math.min(Math.min(buyer.getQuantity()-buyer.getFinalQuantity(),seller.getQuantity()-seller.getFinalQuantity()),maxQuantity-accumQuantity);
            buyer.setFinalQuantity(buyer.getFinalQuantity()+quantityNow);
            seller.setFinalQuantity(seller.getFinalQuantity()+quantityNow);
            ResultPair resultPair = new ResultPair(quantityNow,buyer,seller);
            accumQuantity += quantityNow;
            resultPairs.add(resultPair);
            clearPrice = (buyer.getPrice()+seller.getPrice())/2;
            if (buyer.getFinalQuantity() == buyer.getQuantity() || seller.getFinalQuantity() == seller.getQuantity()){
                if (buyer.getFinalQuantity() == buyer.getQuantity()){
                    i++;
                }
                if (seller.getFinalQuantity() == seller.getQuantity()){
                    j++;
                }
            }
        }
        cr.setClearPrice(clearPrice);
        for (SellerBidAgent seller : sellers) {
            seller.setFinalPrice(clearPrice);
        }
        for (BuyerBidAgent buyer : buyers) {
            buyer.setFinalPrice(clearPrice);
        }
        cr.setResultPairList(resultPairs);
        return cr;
    }

    private List<SellerBidAgent> filterSeller(List<SellerBidAgent> sellerList) {
        return sellerList;
    }

    private List<BuyerBidAgent> filterBuyer(List<BuyerBidAgent> buyerList) {
        return buyerList;
    }


    public static void main(String[] args){
        PXCAgent pxcAgent = new PXCAgent();

        List<BuyerBidAgent> buyerList = new ArrayList<>();
        List<SellerBidAgent> sellerList = new ArrayList<>();

/*        buyerList.add(new BuyerBidAgent(4,10,"01",80.00,-1.00));
        buyerList.add(new BuyerBidAgent(4,10,"02",120.00,-5.00));
        buyerList.add(new BuyerBidAgent(4,10,"03",80.00,-8.00));
        buyerList.add(new BuyerBidAgent(4,10,"04",90.00,-10.00));
        buyerList.add(new BuyerBidAgent(4,10,"05",100.00,-15.00));
        buyerList.add(new BuyerBidAgent(4,10,"06",70.00,-18.00));
        buyerList.add(new BuyerBidAgent(4,10,"07",80.00,-20.00));

        sellerList.add(new SellerBidAgent(4,10,"A",96.00,-100.00));
        sellerList.add(new SellerBidAgent(4,10,"B",96.00,-180.00));
        sellerList.add(new SellerBidAgent(4,10,"C",216.00,-100.00));
        sellerList.add(new SellerBidAgent(4,10,"D",96.00,-180.00));
        sellerList.add(new SellerBidAgent(4,10,"E",96.00,-100.00));*/

        buyerList.add(new BuyerBidAgent(4,10,"01",100.00,50.00));
        buyerList.add(new BuyerBidAgent(4,10,"02",200.00,50.00));
        buyerList.add(new BuyerBidAgent(4,10,"03",220.00,58.00));
        buyerList.add(new BuyerBidAgent(4,10,"04",320.00,61.60));


        sellerList.add(new SellerBidAgent(4,10,"A",200.00,33.20));
        sellerList.add(new SellerBidAgent(4,10,"B",250.00,32.50));
        sellerList.add(new SellerBidAgent(4,10,"C",300.00,42.00));
        sellerList.add(new SellerBidAgent(4,10,"D",300.00,32.00));

        ClearingResult clearingResult = pxcAgent.Clearing(buyerList,sellerList, 800.00);
        List<ResultPair> resultPairs = clearingResult.getResultPairList();
        System.out.println("成交价为"+clearingResult.getClearPrice());
        System.out.println("--------------------------------------------------");
        System.out.println("序号*****seller*****buyer*****成交量*****");
        for (ResultPair result :
                resultPairs) {
            System.out.println("*****"+result.getSeller().getName()+"*****"+result.getBuyer().getName()+"*****"+result.getQuantity()+"*****");
//            System.out.println(result.getSeller().getName()+"的最终成交量为"+result.getSeller().getFinalQuantity()+"***"+result.getSeller().getFinalPrice());
        }
        System.out.println("--------------------------------------------------");
        for (SellerBidAgent seller :
                sellerList) {
            System.out.println(seller.getName()+"的最终成交量为"+seller.getFinalQuantity()+"***"+seller.getFinalPrice()+"初始报量"+seller.getQuantity());
        }
        System.out.println("--------------------------------------------------");
        for (BuyerBidAgent buyer :
                buyerList) {
            System.out.println(buyer.getName()+"的最终成交量为"+buyer.getFinalQuantity()+"***"+buyer.getFinalPrice()+"初始报量"+buyer.getQuantity());
        }

    }
}
