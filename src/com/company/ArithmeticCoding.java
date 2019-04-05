package com.company;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.Map.Entry;

class Interval {
    private BigDecimal left;
    private boolean leftInclusive;
    private BigDecimal right;
    private boolean rightInclusive;

    public Interval() {
        left = BigDecimal.valueOf(0);
        right = BigDecimal.valueOf(1);
        leftInclusive = true;
        rightInclusive = true;
    }

    public Interval(BigDecimal left, boolean leftInclusive, BigDecimal right, boolean rightInclusive) {
        this.left = left;
        this.leftInclusive = leftInclusive;
        this.right = right;
        this.rightInclusive = rightInclusive;
    }

    public BigDecimal getLeft() {
        return left;
    }

    public void setLeft(BigDecimal left) {
        this.left = left;
    }

    public boolean isLeftInclusive() {
        return leftInclusive;
    }

    public void setLeftInclusive(boolean leftInclusive) {
        this.leftInclusive = leftInclusive;
    }

    public BigDecimal getRight() {
        return right;
    }

    public void setRight(BigDecimal right) {
        this.right = right;
    }

    public boolean getRightInclusive() {
        return rightInclusive;
    }

    public void setRightInclusive(boolean rightInclusive) {
        this.rightInclusive = rightInclusive;
    }
}

public class ArithmeticCoding {
    private static final char TERMINATER = '$';
    private String input;
    private Map<Character, Interval> characterIntervalMap = new HashMap<Character, Interval>();

    public ArithmeticCoding(String input, Map<Character, Interval> characterIntervalMap) {
        if (input == null) {
            throw new NullPointerException("input is null");
        }
        if (input.length() == 0) {
            throw new IllegalArgumentException("input'lenght is 0");
        }
        if (!input.endsWith(String.valueOf(TERMINATER))) {
            throw new IllegalArgumentException("input don't end with $");
        }
        this.input = input;
        this.characterIntervalMap = Objects.requireNonNull(characterIntervalMap, "characterIntervalMap is null");
    }

    public ArithmeticCoding(String input) {
        if (input == null) {
            throw new NullPointerException("input is null");
        }
        if (input.length() == 0) {
            throw new IllegalArgumentException("input'lenght is 0");
        }
        if (!input.endsWith(String.valueOf(TERMINATER))) {
            throw new IllegalArgumentException("input don't end with $");
        }
        this.input = input;

        computeCharacterIntervalMap();
    }

    private void computeCharacterIntervalMap() {
        Map<Character, Integer> charCount = count();
        double length = input.length();
        BigDecimal left = new BigDecimal(0);
        for (Entry<Character, Integer> entry : charCount.entrySet()) {
            Character c = entry.getKey();
            Integer count = entry.getValue();
            Interval interval = new Interval();
            interval.setLeft(left);
            interval.setRight(left.add(BigDecimal.valueOf(count / length)));
            interval.setLeftInclusive(true);
            interval.setRightInclusive(false);
            left = interval.getRight();
            characterIntervalMap.put(c, interval);
        }
    }

    private Map<Character, Integer> count() {
        Map<Character, Integer> charCount = new HashMap<>();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            Integer count = charCount.get(c);
            if (count == null) {
                charCount.put(c, 1);
            } else {
                charCount.put(c, count + 1);
            }
        }
        return charCount;
    }

    public String encode() {
        BigDecimal low = new BigDecimal(0);
        BigDecimal high = new BigDecimal(1);
        BigDecimal range = new BigDecimal(1);
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            Interval interval = characterIntervalMap.get(c);
            high = low.add(interval.getRight().multiply(range));
            low = low.add(interval.getLeft().multiply(range));
            range = high.subtract(low);
        }

        Interval resultInterval = new Interval();
        resultInterval.setLeft(low);
        resultInterval.setLeftInclusive(true);
        resultInterval.setRight(high);
        return generateCodeword(resultInterval);
    }

    private String generateCodeword(Interval interval) {
        StringBuilder codeword = new StringBuilder("0.");
        while (binaryStringToBigDecimal(codeword.toString()).compareTo(interval.getLeft()) < 0) {
            codeword.append('1');
            if (binaryStringToBigDecimal(codeword.toString()).compareTo(interval.getRight()) > 0) {
                codeword.setCharAt(codeword.length() - 1, '0');
            }
        }
        return codeword.toString();
    }

    /**
     * @param binaryString 二进制小数(如 0.1001)的字符串形式，只支持大于0小于1的数
     * @return
     */
    private BigDecimal binaryStringToBigDecimal(String binaryString) {
        int dotIndex = binaryString.indexOf('.');
        String decimalPart = binaryString.substring(dotIndex + 1);
        BigDecimal result = new BigDecimal("0");
        if ("".equals(decimalPart)) {
            return result;
        }
        for (int i = 0; i < decimalPart.length(); i++) {
            BigDecimal temp = new BigDecimal(decimalPart.substring(i, i + 1)).multiply(new BigDecimal(1).divide(new BigDecimal(2).pow(i + 1)));
            result = result.add(temp);
        }
        return result;
    }

    /**
     * @param encode
     * @return
     * @throws NullPointerException if encode is null
     */
    public String decode(String encode) {
        if (encode == null) {
            throw new NullPointerException("encode is null");
        }
        BigDecimal value = binaryStringToBigDecimal(encode);
        BigDecimal low;
        BigDecimal high;
        BigDecimal range;
        StringBuilder result = new StringBuilder();
        char lastChar;
        do {
            Entry<Character, Interval> entry = findEntry(value);
            if (entry == null) {
                System.out.println("解码失败，已经解码部分为:" + result.toString());
                return result.toString();
            }
            lastChar = entry.getKey();
            result.append(lastChar);
            low = entry.getValue().getLeft();
            high = entry.getValue().getRight();
            range = high.subtract(low);
            value = value.subtract(low).divide(range, RoundingMode.CEILING);
        } while (lastChar != TERMINATER);
        return result.toString();
    }

    /**
     * @param value
     * @return 没有找到返回null
     */
    private Entry<Character, Interval> findEntry(BigDecimal value) {
        for (Entry<Character, Interval> entry : characterIntervalMap.entrySet()) {
            if (value.compareTo(entry.getValue().getLeft()) > 0
                    && value.compareTo(entry.getValue().getRight()) < 0) {
                return entry;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        Map<Character, Interval> characterIntervalMap = new HashMap<>();
        characterIntervalMap.put('$', new Interval(BigDecimal.valueOf(0), true, BigDecimal.valueOf(0.2), false));
        characterIntervalMap.put('A', new Interval(BigDecimal.valueOf(0.2), true, BigDecimal.valueOf(0.4), false));
        characterIntervalMap.put('B', new Interval(BigDecimal.valueOf(0.4), true, BigDecimal.valueOf(0.6), false));
        characterIntervalMap.put('C', new Interval(BigDecimal.valueOf(0.6), true, BigDecimal.valueOf(0.8), false));
        characterIntervalMap.put('D', new Interval(BigDecimal.valueOf(0.8), true, BigDecimal.valueOf(1), false));
        try {
            ArithmeticCoding code = new ArithmeticCoding("CDBDAC$", characterIntervalMap);
            String encode = code.encode();
            String decode = code.decode(encode);
            System.out.println(code.encode());
            System.out.println(decode);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}