package mcts;

import problem.*;
import simulator.*;


import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class MCTS {
    private ProblemSpec ps;
    public int stepCounter = 0;

    public MCTS(ProblemSpec ps, String output) {
        this.ps = ps;
        int step = 0;
        State s = new State(1, false, false, ps.getFirstCarType(), ProblemSpec.FUEL_MAX,
                TirePressure.ONE_HUNDRED_PERCENT, ps.getFirstDriver(), ps.getFirstTireModel());
        Simulator sim = new Simulator(ps, output);
        Action a;
        while (s.getPos() < ps.getN()) {
            a = findNextMove(s);
            s = sim.step(a);
            if (s != null)
                step += increaseStep(ps, s, a);
            if (a.getActionType().getActionNo() != 1) {
                s = sim.step(new Action(ActionType.MOVE));
                if (s != null)
                    step += increaseStep(ps, s, a);
            }

            if (s == null) {
                System.out.println("Failed attempt. Retrying...");
                step = 0;
                s = new State(1, false, false, ps.getFirstCarType(), ProblemSpec.FUEL_MAX,
                        TirePressure.ONE_HUNDRED_PERCENT, ps.getFirstDriver(), ps.getFirstTireModel());
                sim = new Simulator(ps, output);
            }
        }
        System.out.println("Goal Reached!!!");
        /*BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(output));
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        this.stepCounter = step;
    }

    private int getMillisForCurrentLevel() {
        return 5*ps.getLevel().getLevelNumber()+5;
    }

    public Action findNextMove(State s) {
        long start = System.currentTimeMillis();
        long end = start + 100*getMillisForCurrentLevel();

        Tree tree = new Tree();
        Node rootNode = tree.getRoot();
        rootNode.setState(s);
        rootNode.setChildArray(getPossibleChildren(rootNode));

        while (System.currentTimeMillis() < end) {
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
        return winnerNode.getAction();
    }

    private Node selectPromisingNode(Node rootNode) {
        Node node = rootNode;
        if (node.getVisitCount() < node.getChildArray().size())
            node = node.getRandomChildNode();
        else
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
        double reward;

        MoveSimulator ms = new MoveSimulator();
        State simResult = ms.performA1(ps, node);

        if (simResult.isInBreakdownCondition()){
            reward = - ps.getRepairTime();
        } else if (simResult.isInSlipCondition()){
            reward = - ps.getRepairTime();
        } else {
            reward = simResult.getPos() - node.state.getPos();
        }
        return 0.9*reward;
    }

    public List<Node> getPossibleChildren(Node node) {
        List<ActionType> availableActions = ps.getLevel().getAvailableActions();
        List<Node> possibleNodes = new ArrayList<>();
        State s = node.getState();

        Terrain terrain = ps.getEnvironmentMap()[s.getPos() - 1];
        String car = s.getCarType();
        TirePressure pressure = s.getTirePressure();

        // get fuel consumption
        int terrainIndex = ps.getTerrainIndex(terrain);
        int carIndex = ps.getCarIndex(car);
        int fuelConsumption = ps.getFuelUsage()[terrainIndex][carIndex];

        if (pressure == TirePressure.FIFTY_PERCENT) {
            fuelConsumption *= 3;
        } else if (pressure == TirePressure.SEVENTY_FIVE_PERCENT) {
            fuelConsumption *= 2;
        }
        int currentFuel = s.getFuel();
        if (fuelConsumption > currentFuel) {
            for (int i = 0; i < ps.getCT(); i++) {
                Node tmpNode = new Node();
                tmpNode.setParent(node);
                tmpNode.setAction(new Action(ActionType.CHANGE_CAR,ps.getCarOrder().get(i)));
                tmpNode.setState(s.changeCarType(ps.getCarOrder().get(i)));
                if (!ps.getCarOrder().get(i).equals(node.state.getCarType())) {
                    possibleNodes.add(tmpNode);
                }
            }
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
                        if (!ps.getCarOrder().get(i).equals(node.state.getCarType())) {
                            possibleNodes.add(tmpNode);
                        }
                    }
                } else if (a.getActionNo() == 3) {
                    for (int i = 0; i < ps.getDT(); i++) {
                        Node tmpNode = new Node();
                        tmpNode.setParent(node);
                        tmpNode.setAction(new Action(a,ps.getDriverOrder().get(i)));
                        tmpNode.setState(s.changeDriver(ps.getDriverOrder().get(i)));
                        if (!ps.getDriverOrder().get(i).equals(node.state.getDriver())) {
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

    private int increaseStep(ProblemSpec ps, State s, Action a) {
        if (s.isInSlipCondition())
            return ps.getSlipRecoveryTime();
        if (s.isInBreakdownCondition())
            return ps.getRepairTime();
        if (a.getActionType() == ActionType.ADD_FUEL)
            return a.getFuel() / 10;
        return 1;
    }
}
