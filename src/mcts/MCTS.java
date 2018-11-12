package mcts;

import problem.*;
import simulator.*;


import java.util.ArrayList;
import java.util.List;

public class MCTS {
    static final int WIN_SCORE = 10;
    private ProblemSpec ps;

    public MCTS(ProblemSpec ps) {
        this.ps = ps;
        State s = new State(1, false, false, ps.getFirstCarType(), ProblemSpec.FUEL_MAX,
                TirePressure.ONE_HUNDRED_PERCENT, ps.getFirstDriver(), ps.getFirstTireModel());
        String output = "";
        Simulator sim = new Simulator(ps, output);
        MoveSimulator ms = new MoveSimulator();
        Action a;
        while (s.getPos() < ps.getN()-1) {
            a = findNextMove(s);
            sim.step(a);
            s = ms.PerformAction(s, a);
            sim.step(new Action(ActionType.MOVE));
        }
        System.out.println(output);
    }

    private int getMillisForCurrentLevel() {
        return 3 * ps.getLevel().getLevelNumber() + 10;
    }

    public Action findNextMove(State s) {
        long start = System.currentTimeMillis();
        long end = start + 30*getMillisForCurrentLevel();

        Tree tree = new Tree();
        Node rootNode = tree.getRoot();
        rootNode.setState(s.copyState().getStartState(ps.getFirstCarType(),
                ps.getFirstDriver(), ps.getFirstTireModel()));
        rootNode.setChildArray(getPossibleChildren(rootNode));

        while (System.currentTimeMillis() < end) {
            // Phase 1 - Selection
            Node promisingNode = selectPromisingNode(rootNode);

            // Phase 2 - Expansion
            getPossibleChildren(promisingNode);

            // Phase 3 - Simulation
            Node nodeToExplore = promisingNode;
            nodeToExplore = promisingNode.getRandomChildNode();
            double playoutResult = simulateRandomPlayout(nodeToExplore);

            // Phase 4 - Update
            backPropagation(nodeToExplore, playoutResult);
        }

        Node winnerNode = rootNode.getChildWithMaxScore();
        tree.setRoot(winnerNode);
        return winnerNode.getAction();
    }

    private Node selectPromisingNode(Node rootNode) {
        Node node = rootNode;
        node = UCT.findBestNodeWithUCT(node);
        return node;
    }

    private void backPropagation(Node nodeToExplore, double playoutResult) {
        Node tempNode = nodeToExplore;
        while (tempNode != null) {
            tempNode.incrementVisit();
            tempNode.addScore(playoutResult); //TODO??
            tempNode = tempNode.getParent();
        }
    }

    private double simulateRandomPlayout(Node node) {
        MoveSimulator ms = new MoveSimulator();
        State simResult = ms.performA1(ps, node);
        if (simResult.isInBreakdownCondition()){
            node.reward -= 3;
        } else if (simResult.isInSlipCondition()){
            node.reward -= 3;
        } else {
            node.reward += simResult.getPos();
        }
        return node.reward;
    }

    public List<Node> getPossibleChildren(Node node) {
        List<ActionType> availableActions = ps.getLevel().getAvailableActions();
        List<Node> possibleNodes = new ArrayList<>();
        State s = node.getState();

        for (ActionType a : availableActions ) {
            Node tmpNode = new Node();
            tmpNode.setParent(node);

            if (a.getActionNo() == 2) {
                for (int i = 0; i < ps.getCT(); i++) {
                    tmpNode.setAction(new Action(a,ps.getCarOrder().get(i)));
                    tmpNode.setState(s.changeCarType(ps.getCarOrder().get(i)));
                    if (ps.getCarOrder().get(i) != node.state.getCarType()) {
                        possibleNodes.add(tmpNode);
                    }
                }
            } else if (a.getActionNo() == 3) {
                for (int i = 0; i < ps.getDT(); i++) {
                    tmpNode.setAction(new Action(a,ps.getDriverOrder().get(i)));
                    tmpNode.setState(s.changeDriver(ps.getDriverOrder().get(i)));
                    if (ps.getDriverOrder().get(i) != node.state.getDriver()) {
                        possibleNodes.add(tmpNode);
                    }
                }
            } else if (a.getActionNo() == 4) {
                for (int i = 0; i < ps.NUM_TYRE_MODELS; i++) {
                    tmpNode.setAction(new Action(a,ps.getTireOrder().get(i)));
                    tmpNode.setState(s.changeTires(ps.getTireOrder().get(i)));
                    if (ps.getTireOrder().get(i) != node.state.getTireModel()) {
                        possibleNodes.add(tmpNode);
                    }
                }
            } else if (a.getActionNo() == 5) {
                tmpNode.setAction(new Action(a,50 - s.getFuel()));
                tmpNode.setState(s.addFuel(50 - s.getFuel()));
                possibleNodes.add(tmpNode);
            } else if (a.getActionNo() == 6) {
                TirePressure NewPressure = TirePressure.FIFTY_PERCENT;
                if (NewPressure != node.state.getTirePressure()) {
                    tmpNode.setAction(new Action(a,NewPressure));
                    tmpNode.setState(s.changeTirePressure(TirePressure.FIFTY_PERCENT));
                    possibleNodes.add(tmpNode);
                }
                NewPressure = TirePressure.SEVENTY_FIVE_PERCENT;
                if (NewPressure != node.state.getTirePressure()) {
                    tmpNode.setAction(new Action(a,NewPressure));
                    tmpNode.setState(s.changeTirePressure(TirePressure.SEVENTY_FIVE_PERCENT));
                    possibleNodes.add(tmpNode);
                }
                NewPressure = TirePressure.ONE_HUNDRED_PERCENT;
                if (NewPressure != node.state.getTirePressure()) {
                    tmpNode.setAction(new Action(a,NewPressure));
                    tmpNode.setState(s.changeTirePressure(TirePressure.ONE_HUNDRED_PERCENT));
                    possibleNodes.add(tmpNode);
                }
            }

        }
        node.setChildArray(possibleNodes);
        return possibleNodes;
    }
}
