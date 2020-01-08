package client;

import utils.HashCracker;

public class Range {
    public String from;
    public String to;
    public long inRange;

    public Range(String from, String to, int len) {
        this.inRange = HashCracker.alphabeticToDecimal(to) - HashCracker.alphabeticToDecimal(from) + 1;
        this.from = String.format("%1$" + len + "s", from).replace(' ', 'a');
        this.to = String.format("%1$" + len + "s", to).replace(' ', 'a');
    }
}
