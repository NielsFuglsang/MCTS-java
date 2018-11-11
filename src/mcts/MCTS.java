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
        System.out.println(s);
        for (int i = 0; i < 10; i++) {
            s = findNextMove(s);
            //if (MoveSimulator.isTerminal(s))
            //    System.out.println("GOAL REACHED!");
            System.out.println(s);
        }
    }

    private int getMillisForCurrentLevel() {
        return 5* ps.getLevel().getLevelNumber() + 10;
    }

    public State findNextMove(State s) {
        long start = System.currentTimeMillis();
        long end = start + getMillisForCurrentLevel();

        Tree tree = new Tree();
        Node rootNode = tree.getRoot();
        rootNode.setState(s.copyState().getStartState(ps.getFirstCarType(),
                ps.getFirstDriver(), ps.getFirstTireModel()));
        rootNode.setChildArray(getPossibleChildren(rootNode));

        //while (System.currentTimeMillis() < end) {
            // Phase 1 - Selection
            Node promisingNode = selectPromisingNode(rootNode);
            System.out.println(promisingNode);

            // Phase 2 - Expansion
            getPossibleChildren(promisingNode);

            // Phase 3 - Simulation
            Node nodeToExplore = promisingNode;
            nodeToExplore = promisingNode.getRandomChildNode();
            int playoutResult = simulateRandomPlayout(nodeToExplore);

            // Phase 4 - Update
            backPropagation(nodeToExplore, playoutResult);
        //}

        Node winnerNode = rootNode.getChildWithMaxScore();
        tree.setRoot(winnerNode);
        return winnerNode.getState();
    }

    private Node selectPromisingNode(Node rootNode) {
        Node node = rootNode;
        node = UCT.findBestNodeWithUCT(node);
        return node;
    }

    private void backPropagation(Node nodeToExplore, int playoutResult) {
        Node tempNode = nodeToExplore;
        while (tempNode != null) {
            tempNode.incrementVisit();
            tempNode.addScore(playoutResult); //TODO??
            tempNode = tempNode.getParent();
        }
    }

    private int simulateRandomPlayout(Node node) {
        MoveSimulator ms = new MoveSimulator();
        State simResult = ms.performA1(ps, node);
        return simResult.getPos();
    }

    public List<Node> getPossibleChildren(Node node) {
        List<ActionType> availableActions = ps.getLevel().getAvailableActions();
        List<Node> possibleNodes = new ArrayList<>();

        Node tmpNode = new Node();

        for (ActionType a : availableActions ) {
            tmpNode.setParent(node);
            State s = node.getState();
            switch (a) {
                case CHANGE_CAR:
                    for (int i = 0; i < ps.getCT(); i++) {
                        tmpNode.setState(s.changeCarType(ps.getCarOrder().get(i)));
                        if (ps.getCarOrder().get(i) != node.state.getCarType()) {
                            possibleNodes.add(tmpNode);
                        }
                    }
                case CHANGE_DRIVER:
                    for (int i = 0; i < ps.getDT(); i++) {
                        tmpNode.setState(s.changeDriver(ps.getDriverOrder().get(i)));
                        if (ps.getDriverOrder().get(i) != node.state.getDriver()) {
                            possibleNodes.add(tmpNode);
                        }
                    }
                case CHANGE_TIRES:
                    for (int i = 0; i < ps.NUM_TYRE_MODELS; i++) {
                        tmpNode.setState(s.changeTires(ps.getTireOrder().get(i)));
                        if (ps.getTireOrder().get(i) != node.state.getTireModel()) {
                            possibleNodes.add(tmpNode);
                        }
                    }
                case ADD_FUEL:
                    tmpNode.setState(s.addFuel(50-tmpNode.state.getFuel()));
                    possibleNodes.add(tmpNode);
                case CHANGE_PRESSURE:
                    if (TirePressure.FIFTY_PERCENT != node.state.getTirePressure()) {
                        tmpNode.setState(s.changeTirePressure(TirePressure.FIFTY_PERCENT));
                        possibleNodes.add(tmpNode);
                    }
                    if (TirePressure.SEVENTY_FIVE_PERCENT != node.state.getTirePressure()) {
                        tmpNode.setState(s.changeTirePressure(TirePressure.SEVENTY_FIVE_PERCENT));
                        possibleNodes.add(tmpNode);
                    }
                    if (TirePressure.ONE_HUNDRED_PERCENT != node.state.getTirePressure()) {
                        tmpNode.setState(s.changeTirePressure(TirePressure.ONE_HUNDRED_PERCENT));
                        possibleNodes.add(tmpNode);
                    }
                default:
                //CHANGE_CAR_AND_DRIVER,
                //CHANGE_TIRE_FUEL_PRESSURE;
            }
        }
        node.setChildArray(possibleNodes);
        return possibleNodes;
    }
}
