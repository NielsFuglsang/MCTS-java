package mcts;

import problem.*;
import simulator.*;


import java.util.ArrayList;
import java.util.List;

public class MCTS {
    private ProblemSpec ps;

    public MCTS(ProblemSpec ps) {
        this.ps = ps;
        State s = new State(1, false, false, ps.getFirstCarType(), ProblemSpec.FUEL_MAX,
                TirePressure.ONE_HUNDRED_PERCENT, ps.getFirstDriver(), ps.getFirstTireModel());
        String output = "";
        Simulator sim = new Simulator(ps, output);
        Action a;
        while (s.getPos() < ps.getN()-1) {
            a = findNextMove(s);
            sim.step(a);
            s = sim.step(new Action(ActionType.MOVE));
        }
        System.out.println(output);
    }

    private int getMillisForCurrentLevel() {
        return 10 * ps.getLevel().getLevelNumber() + 3;
    }

    public Action findNextMove(State s) {
        long start = System.currentTimeMillis();
        long end = start + 60*getMillisForCurrentLevel();

        Tree tree = new Tree();
        Node rootNode = tree.getRoot();
        rootNode.setState(s);
        rootNode.setChildArray(getPossibleChildren(rootNode));

        //while (System.currentTimeMillis() < end) {
        for (int i = 0; i < 5; i++) {
            // Phase 1 - Selection
            Node promisingNode = selectPromisingNode(rootNode);

            // Phase 2 - Expansion
            getPossibleChildren(promisingNode);

            // Phase 3 - Simulation
            Node nodeToExplore;
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
            tempNode.addScore(playoutResult);
            tempNode = tempNode.getParent();
        }
    }

    private double simulateRandomPlayout(Node node) {
        double reward = 0.0;
        if (node.state.getFuel() < 10) {
            reward = -100;
        }
        MoveSimulator ms = new MoveSimulator();
        State simResult = ms.performA1(ps, node);

        if (simResult.isInBreakdownCondition()){

        } else if (simResult.isInSlipCondition()){

        } else {
            if (simResult.getPos() > node.state.getPos())
                reward = 1;
        }
        return reward;
    }

    public List<Node> getPossibleChildren(Node node) {
        List<ActionType> availableActions = ps.getLevel().getAvailableActions();
        List<Node> possibleNodes = new ArrayList<>();
        State s = node.getState();

        if (node.state.getFuel() < 10) {
            Node tmpNode = new Node();
            tmpNode.setParent(node);
            tmpNode.setAction(new Action(ActionType.ADD_FUEL,50 - s.getFuel()));
            tmpNode.setState(s.addFuel(50 - s.getFuel()));
            possibleNodes.add(tmpNode);
        } else {
            for (ActionType a : availableActions ) {
                if (a.getActionNo() == 1) {
                    Node tmpNode = new Node();
                    tmpNode.setParent(node);
                    tmpNode.setAction(new Action(a));
                    tmpNode.setState(s);
                    possibleNodes.add(tmpNode);
                } else if (a.getActionNo() == 2) {
                    for (int i = 0; i < ps.getCT(); i++) {
                        Node tmpNode = new Node();
                        tmpNode.setParent(node);
                        tmpNode.setAction(new Action(a,ps.getCarOrder().get(i)));
                        tmpNode.setState(s.changeCarType(ps.getCarOrder().get(i)));
                        if (ps.getCarOrder().get(i) != node.state.getCarType()) {
                            possibleNodes.add(tmpNode);
                        }
                    }
                } else if (a.getActionNo() == 3) {
                    for (int i = 0; i < ps.getDT(); i++) {
                        Node tmpNode = new Node();
                        tmpNode.setParent(node);
                        tmpNode.setAction(new Action(a,ps.getDriverOrder().get(i)));
                        tmpNode.setState(s.changeDriver(ps.getDriverOrder().get(i)));
                        if (ps.getDriverOrder().get(i) != node.state.getDriver()) {
                            possibleNodes.add(tmpNode);
                        }
                    }
                } else if (a.getActionNo() == 4) {
                    for (int i = 0; i < ps.NUM_TYRE_MODELS; i++) {
                        Node tmpNode = new Node();
                        tmpNode.setParent(node);
                        tmpNode.setAction(new Action(a,ps.getTireOrder().get(i)));
                        tmpNode.setState(s.changeTires(ps.getTireOrder().get(i)));
                        if (ps.getTireOrder().get(i) != node.state.getTireModel()) {
                            possibleNodes.add(tmpNode);
                        }
                    }
                } else if (a.getActionNo() == 5) {
                    Node tmpNode = new Node();
                    tmpNode.setParent(node);
                    tmpNode.setAction(new Action(a,50 - s.getFuel()));
                    tmpNode.setState(s.addFuel(50 - s.getFuel()));
                    possibleNodes.add(tmpNode);
                } else if (a.getActionNo() == 6) {
                    TirePressure NewPressure = TirePressure.FIFTY_PERCENT;
                    if (NewPressure != node.state.getTirePressure()) {
                        Node tmpNode = new Node();
                        tmpNode.setParent(node);
                        tmpNode.setAction(new Action(a,NewPressure));
                        tmpNode.setState(s.changeTirePressure(TirePressure.FIFTY_PERCENT));
                        possibleNodes.add(tmpNode);
                    }
                    NewPressure = TirePressure.SEVENTY_FIVE_PERCENT;
                    if (NewPressure != node.state.getTirePressure()) {
                        Node tmpNode = new Node();
                        tmpNode.setParent(node);
                        tmpNode.setAction(new Action(a,NewPressure));
                        tmpNode.setState(s.changeTirePressure(TirePressure.SEVENTY_FIVE_PERCENT));
                        possibleNodes.add(tmpNode);
                    }
                    NewPressure = TirePressure.ONE_HUNDRED_PERCENT;
                    if (NewPressure != node.state.getTirePressure()) {
                        Node tmpNode = new Node();
                        tmpNode.setParent(node);
                        tmpNode.setAction(new Action(a,NewPressure));
                        tmpNode.setState(s.changeTirePressure(TirePressure.ONE_HUNDRED_PERCENT));
                        possibleNodes.add(tmpNode);
                    }
                }

            }
        }
        node.setChildArray(possibleNodes);
        return possibleNodes;
    }
}
