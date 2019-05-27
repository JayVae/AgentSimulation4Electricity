package com.xjtu.power.agent.engine;

import com.xjtu.power.agent.TransactionMemberAgent.BuyerBidAgent;
import com.xjtu.power.agent.TransactionMemberAgent.ISOAgent;
import com.xjtu.power.agent.TransactionMemberAgent.PXCAgent;
import com.xjtu.power.agent.TransactionMemberAgent.SellerBidAgent;
import com.xjtu.power.agent.algorithm.learning.qlearn.QAgent;
import com.xjtu.power.dataParam.ClearingResult;
import com.xjtu.power.dataParam.ResultPair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.xjtu.power.tool.NewTxt.arrOut;

/**
 * @Author: Jay
 * @Date: Created in 15:18 2019/4/11
 * @Modified By:
 * 将要仿真的信息传递到这里[即init]，初始化信息，然后进行迭代，并且保存每一步的结果
 */
public class Simulation
{
    private List<BuyerBidAgent> buyerBidAgents = new ArrayList<>();

    private List<SellerBidAgent> sellerBidAgents = new ArrayList<>();

    private int iterNum = 1000;

    private int simulationId;

//    存储整体结果，即随着轮数变化的结果，分别是出清价格，出清电量，购电方平均申报价格，发电方平均申报价格,第一个购电方的利润，第一个售电方的利润
    private double[][] wholeResult = new double[6][200];
    private ClearingResult clearingResultWhole ;


    public void simulation() throws IOException {

        init();

        for (int i = 0; i < iterNum; i++) {

            System.out.println("*****************************************");

/*            if (i == iterNum*0.6){
                System.out.println("1234567890。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。");
            }*/

            PXCAgent pxcAgent = new PXCAgent();
//            todo
            ClearingResult clearingResult = pxcAgent.Clearing(buyerBidAgents,sellerBidAgents,800.00);

            double buyerQ = 0.0;
            double sellerQ = 0.0;
            double buyerQP = 0.0;
            double sellerQP = 0.0;
            double buyer1Reward = 0.0;
            double seller1Reward = 0.0;
            double sellerQchengjiao = 0.0;
            double clearPrice = clearingResult.getClearPrice();
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!"+clearPrice);
            clearingResultWhole = clearingResult;
            System.out.println("**********buyer***********");
            for (BuyerBidAgent buyBidAgent :
                    buyerBidAgents) {
                QAgent agent = buyBidAgent.getAgent();
                System.out.println(buyBidAgent.getName());
                int actionId = agent.selectAction().getIndex();
                System.out.println("Agent does action：" + actionId);

//                当前状态在哪里？？？
                int newStateId = buyBidAgent.update( actionId);

                double reward = buyBidAgent.reward();

                System.out.println("Now the new state is " + newStateId);
                System.out.println("Agent receives Reward = " + reward);

                agent.update(actionId, newStateId, reward);

//                将finalQuantity改为0
/*                if (i == iterNum*0.6){
                    agent.setEpslon(0.1);
                }*/
                if (i<iterNum-1){
                    buyBidAgent.writeBack();
                }
                if (buyBidAgent.getName() == "01"){
                    buyer1Reward = reward;
                }else {
                }

                buyerQ += buyBidAgent.getQuantity();
                buyerQP += buyBidAgent.getPrice()*buyBidAgent.getQuantity();

            }
            System.out.println("@@@@@@@@@@seller@@@@@@@@@@");
            for (SellerBidAgent sellerBidAgent:
                 sellerBidAgents) {
                QAgent agent = sellerBidAgent.getAgent();
                int actionId = agent.selectAction().getIndex();
                System.out.println("Agent does action:" + actionId);

                int newStateId = sellerBidAgent.update( actionId);

                double reward = sellerBidAgent.reward();

                System.out.println("Now the new state is " + newStateId);
                System.out.println("Agent receives Reward = " + reward);

                agent.update(actionId, newStateId, reward);

/*                if (i == iterNum*0.6){
                    agent.setEpslon(0.1);
                }*/
                sellerQchengjiao += sellerBidAgent.getFinalQuantity();
                if (i<iterNum-1){
                    sellerBidAgent.writeBack();
                }
                if (sellerBidAgent.getName() == "A"){
                    seller1Reward = reward;
                }else {
                }
                sellerQ += sellerBidAgent.getQuantity();
                sellerQP += sellerBidAgent.getQuantity()*sellerBidAgent.getPrice();
            }

            safetyCheck();
            if ((i%5)==0){
                wholeResult[0][i/5] = clearPrice;
                wholeResult[1][i/5] = sellerQchengjiao;
                wholeResult[2][i/5] = buyerQP/buyerQ;
                wholeResult[3][i/5] = sellerQP/sellerQ;
                wholeResult[4][i/5] = buyer1Reward;
                wholeResult[5][i/5] = seller1Reward;
                System.out.println("buyer:"+buyerQ);
                System.out.println("seller"+sellerQ);
            }
//分别是出清价格，出清电量，购电方平均申报价格，发电方平均申报价格

            saveThisResult();
            System.out.println("第"+i+"结束，出清结果为"+clearPrice);
        }

        System.out.println("--------------------------------------------------");

        System.out.println("成交价为"+clearingResultWhole.getClearPrice());
        for (SellerBidAgent seller :
                sellerBidAgents) {
            System.out.println(seller.getName()+"的最终成交量为"+seller.getFinalQuantity()+"***"+seller.getFinalPrice()+"初始报量"+seller.getQuantity());
        }
        System.out.println("--------------------------------------------------");
        for (BuyerBidAgent buyer :
                buyerBidAgents) {
            System.out.println(buyer.getName()+"的最终成交量为"+buyer.getFinalQuantity()+"***"+buyer.getFinalPrice()+"初始报量"+buyer.getQuantity());
        }

        System.out.println("--------------------------------------------------");
        System.out.println("序号*****seller*****buyer*****成交量*****");
        List<ResultPair> resultPairs = clearingResultWhole.getResultPairList();
        for (ResultPair result :
                resultPairs) {
            System.out.println("*****"+result.getSeller().getName()+"*****"+result.getBuyer().getName()+"*****"+result.getQuantity()+"*****");
//            System.out.println(result.getSeller().getName()+"的最终成交量为"+result.getSeller().getFinalQuantity()+"***"+result.getSeller().getFinalPrice());
        }

        arrOut(wholeResult);
        saveFinalResult();

        showFinalResultTable();
    }

    public void showFinalResultTable() {

    }

    public void safetyCheck() {
        ISOAgent isoAgent = new ISOAgent();
        isoAgent.safetyCheck();
    }

    private void saveFinalResult() {
    }

    private void saveThisResult() {

    }

    public void init(){

        buyerBidAgents.add(new BuyerBidAgent(20,10,"01",100.00,50.00,60,0.05));
        buyerBidAgents.add(new BuyerBidAgent(20,10,"02",200.00,50.00,70,0.05));
        buyerBidAgents.add(new BuyerBidAgent(20,10,"03",220.00,58.00,80,0.05));
        buyerBidAgents.add(new BuyerBidAgent(20,10,"04",320.00,61.60,100,0.06));


        sellerBidAgents.add(new SellerBidAgent(20,10,"A",200.00,33.20,0.028,22,0));
        sellerBidAgents.add(new SellerBidAgent(20,10,"B",250.00,32.50,0.025,20,0));
        sellerBidAgents.add(new SellerBidAgent(20,10,"C",300.00,42.00,0.035,21,0));
        sellerBidAgents.add(new SellerBidAgent(20,10,"D",300.00,32.00,0.025,17,0));
    }

    public void addBuyerBideAgent(){

    }

    public void setBuyerBidAgents(List<BuyerBidAgent> buyerBidAgents) {
        this.buyerBidAgents = buyerBidAgents;
    }

    public void setSellerBidAgents(List<SellerBidAgent> sellerBidAgents) {
        this.sellerBidAgents = sellerBidAgents;
    }

    public static void main(String[] args) throws IOException {
        Simulation simulation = new Simulation();
        simulation.simulation();
    }
}
