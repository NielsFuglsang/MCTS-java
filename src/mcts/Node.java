package mcts;

import problem.Action;
import problem.ActionType;
import problem.Level;
import problem.TirePressure;
import simulator.State;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Node {
    public State state;
    public Node parent;
    public List<Node> childArray;
    public double reward;
    public int count;
    public Action action;

    public Node() {
        this.state = null;
        this.parent = null;
        this.childArray = null;
        this.reward = 0;
        this.count = 0;
    }

    public Node(State state) {
        this.state = state;
        this.childArray = new ArrayList<>();
        this.reward = 0;
        this.count = 0;
    }

    public Node(State state, Node parent, List<Node> childArray) {
        this.state = state;
        this.parent = parent;
        this.childArray = childArray;
        this.reward = 0;
        this.count = 0;
    }

    public Node(Node node) {
        this.childArray = new ArrayList<>();
        this.reward = 0;
        this.count = 0;
        State s = node.getState();
        this.state = new State( s.getPos(),
                s.isInSlipCondition(),
                s.isInBreakdownCondition(),
                s.getCarType(),
                s.getFuel(),
                s.getTirePressure(),
                s.getDriver(),
                s.getTireModel() );
        if (node.getParent() != null)
            setParent(node.getParent());
        List<Node> childArray = node.getChildArray();
        for (Node child : childArray) {
            this.childArray.add(new Node(child));
        }
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public List<Node> getChildArray() {
        return childArray;
    }

    public void setChildArray(List<Node> childArray) {
        this.childArray = childArray;
    }

    public Node getRandomChildNode() {
        int noOfPossibleMoves = this.childArray.size();
        int selectRandom = (int) (Math.random() * noOfPossibleMoves);
        return this.childArray.get(selectRandom);
    }

    public Node getChildWithMaxScore() {
        return Collections.max(this.childArray, Comparator.comparing(c -> {
            return c.getVisitCount();
        }));
    }

    public int getVisitCount() { return count; }

    public void setVisitCount(int visitCount) { this.count = visitCount; }

    public double getWinScore() { return reward; }

    public void setWinScore(double winScore) { this.reward = winScore; }

    public void incrementVisit() { this.count++; }

    public void addScore(double score) {
        if (this.reward != Integer.MIN_VALUE)
            this.reward += score;
    }

    public void setAction(Action a) { this.action = a; }

    public Action getAction() { return this.action; }

} 