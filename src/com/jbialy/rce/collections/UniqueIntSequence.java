package com.jbialy.rce.collections;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class UniqueIntSequence {
    private final double expandFactor = 0.1;
    private int[] sequences;
    private int reservedIndexes;

    public UniqueIntSequence(int firstValue, int lastValue, List<Integer> except) {
        this.sequences = new int[]{firstValue, lastValue};
        this.reservedIndexes = 2;

        ArrayList<Integer> sorted = new ArrayList<>(new HashSet<>(except));
        sorted.sort(Integer::compareTo);

        int[] containersToCut = sortedArrayToRangeContainers(listToArray(sorted));

        for (int i = 0; i < containersToCut.length - 1; i += 2) {
            int cutRangeFirstValue = containersToCut[i];
            int cutRangeLastValue = containersToCut[i + 1];

            cutRange(cutRangeFirstValue, cutRangeLastValue);
        }
    }

    public UniqueIntSequence(int firstValue, int lastValue) {
        this.sequences = new int[]{firstValue, lastValue};
        this.reservedIndexes = 2;
    }

    public UniqueIntSequence(UniqueIntSequence base) {
        this.sequences = new int[base.sequences.length];
        this.reservedIndexes = base.reservedIndexes;
        System.arraycopy(base.sequences, 0, this.sequences, 0, this.sequences.length);
    }

    public UniqueIntSequence(int initialCapacity) {
        this.sequences = new int[initialCapacity];
        this.reservedIndexes = 0;
    }

    private UniqueIntSequence(int[] sequences) {
        this.sequences = sequences;
        this.reservedIndexes = sequences.length;
    }

    private UniqueIntSequence() {
        this.sequences = new int[0];
        this.reservedIndexes = 0;
    }

    private static int[] sortedArrayToRangeContainers(int[] intArray) {
        int[] tmpResult = new int[intArray.length * 2];
        int tmpResultCurrentRawLeftIndex = 0;
        int lastValue = intArray[0];
        tmpResult[tmpResultCurrentRawLeftIndex] = lastValue;
        tmpResult[tmpResultCurrentRawLeftIndex + 1] = lastValue;

        for (int i = 1; i < intArray.length; i++) {
            int currentValue = intArray[i];

            if (currentValue != lastValue + 1) {
                tmpResultCurrentRawLeftIndex += 2;
                tmpResult[tmpResultCurrentRawLeftIndex] = currentValue;
            }
            tmpResult[tmpResultCurrentRawLeftIndex + 1] = currentValue;

            lastValue = currentValue;
        }

        int[] trimmedResult = new int[tmpResultCurrentRawLeftIndex + 2];
        System.arraycopy(tmpResult, 0, trimmedResult, 0, trimmedResult.length);

        return trimmedResult;
    }

    public static UniqueIntSequence empty() {
        return new UniqueIntSequence();
    }

//    private static int rawIndexToSeqIndex(int rawIndex) {
//        return rawIndex / 2;
//    }

    private static int[] listToArray(List<Integer> inputArray) {
        int[] result = new int[inputArray.size()];

        for (int i = 0; i < inputArray.size(); i++) {
            result[i] = inputArray.get(i);
        }

        return result;
    }

    private int getIndexOfLastContainerStartingBefore(int val) {
        final int lastSequenceLeftIndex = this.reservedIndexes - 2;
        int maxSeqFirstValue = this.sequences[lastSequenceLeftIndex];
        if (maxSeqFirstValue < val) return lastSequenceLeftIndex;

        int minSeqFirstValue = this.sequences[0];
        if (minSeqFirstValue >= val) return -1;

        int minSeqLeftIndex = 0;
        int maxSeqLeftIndex = lastSequenceLeftIndex;

        while (maxSeqLeftIndex - minSeqLeftIndex != 0) {
            int midSeqLeftIndex = ((minSeqLeftIndex + maxSeqLeftIndex) >> 2) << 1;
            int midSeqFirstValue = this.sequences[midSeqLeftIndex];

            if (midSeqFirstValue < val && midSeqLeftIndex != minSeqLeftIndex) {
                minSeqLeftIndex = midSeqLeftIndex;
            } else if (midSeqFirstValue > val && midSeqLeftIndex != maxSeqLeftIndex) {
                maxSeqLeftIndex = midSeqLeftIndex;
            } else {
                int seqAfterMidLeftIndex = midSeqLeftIndex + 2;
                int seqAfterMidFirstValue = this.sequences[seqAfterMidLeftIndex];

                if (val > seqAfterMidFirstValue) {
                    return seqAfterMidLeftIndex;
                } else {
                    if (val > midSeqFirstValue) {
                        return midSeqLeftIndex;
                    } else {
                        int seqBeforeMidLeftIndex = midSeqLeftIndex - 2;
                        int seqBeforeMidFirstValue = this.sequences[seqBeforeMidLeftIndex];

                        if (val > seqBeforeMidFirstValue) {
                            return seqBeforeMidLeftIndex;
                        } else {
                            return -1;
                        }
                    }
                }
            }
        }

        return minSeqLeftIndex;
    }

    private int getIndexOfFirstSequenceEndingAfter(int val) {
        int minContainerLastValue = this.sequences[1];
        if (minContainerLastValue > val) return 0;

        final int lastSeqRightIndex = this.reservedIndexes - 1;
        int maxContainerLastValue = this.sequences[lastSeqRightIndex];
        if (maxContainerLastValue <= val) return lastSeqRightIndex + 1; //out of range

        int minContainerLeftIndex = 0;
        int maxContainerLeftIndex = lastSeqRightIndex - 1;

        while (maxContainerLeftIndex - minContainerLeftIndex > 0) {
            int midIndex = ((minContainerLeftIndex + maxContainerLeftIndex) >> 2) << 1;
            int lastValueOfMidContainer = this.sequences[midIndex + 1];

            if (lastValueOfMidContainer < val) {
                minContainerLeftIndex = midIndex + 2;
            } else if (lastValueOfMidContainer > val) {
                maxContainerLeftIndex = midIndex;
            } else {
                int t = 2;
            }
        }

        return minContainerLeftIndex;
    }

    private int getIndexOfLastContainerEndingBefore(int val) {
        final int lastSequenceLeftIndex = this.reservedIndexes - 2;
        int maxSeqLastValue = this.sequences[lastSequenceLeftIndex + 1];
        if (maxSeqLastValue < val) return lastSequenceLeftIndex;

        int minSeqLastValue = this.sequences[1];
        if (minSeqLastValue >= val) return -1;

        int minSeqLeftIndex = 0;
        int maxSeqLeftIndex = lastSequenceLeftIndex;

        while (maxSeqLeftIndex - minSeqLeftIndex != 0) {
            int midSeqLeftIndex = ((minSeqLeftIndex + maxSeqLeftIndex) >> 2) << 1;
            int lastValueOfMidSeq = this.sequences[midSeqLeftIndex + 1];

            if (lastValueOfMidSeq < val && midSeqLeftIndex != minSeqLeftIndex) {
                minSeqLeftIndex = midSeqLeftIndex;
            } else if (lastValueOfMidSeq > val && midSeqLeftIndex != maxSeqLeftIndex) {
                maxSeqLeftIndex = midSeqLeftIndex;
            } else {
                int seqAfterMidLeftIndex = midSeqLeftIndex + 2;
                int seqAfterMidLastValue = this.sequences[seqAfterMidLeftIndex + 1];

                if (val > seqAfterMidLastValue) {
                    return seqAfterMidLeftIndex;
                } else {
                    if (val > lastValueOfMidSeq) {
                        return midSeqLeftIndex;
                    } else {
                        int seqBeforeMidLeftIndex = midSeqLeftIndex - 2;
                        int seqBeforeMidLastValue = this.sequences[seqBeforeMidLeftIndex + 1];

                        if (val > seqBeforeMidLastValue) {
                            return seqBeforeMidLeftIndex;
                        } else {
                            return -1;
                        }
                    }
                }
            }
        }

        return minSeqLeftIndex;
    }

    private int getIndexOfFirstContainerStartingAfter(int val) {
        final int lastSequenceLeftIndex = this.reservedIndexes - 2;
        int lastSeqFirstValue = this.sequences[lastSequenceLeftIndex];
        if (lastSeqFirstValue <= val) return lastSequenceLeftIndex + 2;

        int minSeqFirstValue = this.sequences[0];
        if (minSeqFirstValue > val) return 0;

        int minSeqLeftIndex = 0;
        int maxSeqLeftIndex = lastSequenceLeftIndex;

        while (maxSeqLeftIndex - minSeqLeftIndex != 0) {
            final int midSeqLeftIndex = ((minSeqLeftIndex + maxSeqLeftIndex) >> 1) & (0xfffffffe);

            int firstValueOfMidSeq = this.sequences[midSeqLeftIndex];

            if (firstValueOfMidSeq < val && midSeqLeftIndex != minSeqLeftIndex) {
                minSeqLeftIndex = midSeqLeftIndex;
            } else if (firstValueOfMidSeq > val && midSeqLeftIndex != maxSeqLeftIndex) {
                maxSeqLeftIndex = midSeqLeftIndex;
            } else {
                boolean seqBeforeExists = midSeqLeftIndex >= 2;
                int seqBeforeMidLeftIndex = midSeqLeftIndex - 2;

                if (seqBeforeExists && this.sequences[seqBeforeMidLeftIndex] > val) {
                    return seqBeforeMidLeftIndex;
                } else {
                    if (firstValueOfMidSeq > val) {
                        return midSeqLeftIndex;
                    } else {
                        boolean seqAfterMidExists = midSeqLeftIndex + 2 < this.reservedIndexes - 1;
                        int seqAfterMidLeftIndex = midSeqLeftIndex + 2;
                        if (seqAfterMidExists && this.sequences[seqAfterMidLeftIndex] > val) {
                            return seqAfterMidLeftIndex;
                        } else {
                            return lastSequenceLeftIndex + 2; //probably bug?
                        }
                    }
                }
            }
        }

        if (this.sequences[minSeqLeftIndex] > val) {
            return minSeqLeftIndex;
        } else {
            return lastSequenceLeftIndex + 2;
        }
    }

    private int min(int a, int b) {
        if (a <= b) return a;
        return b;
    }

    private int getRawLeftIndexOfSubsequenceContaining(int value) {
        int minSeqRawLeftIndex = 0;
        int maxSeqRawLeftIndex = this.reservedIndexes - 2;

        while (maxSeqRawLeftIndex - minSeqRawLeftIndex > 2) {
            int middleSeqRawLeftIndex = ((minSeqRawLeftIndex + maxSeqRawLeftIndex) >> 2) << 1; // ((minSeqRawLeftIndex + maxSeqRawLeftIndex) / 4) * 2
            int middleSeqFirstValue = this.sequences[middleSeqRawLeftIndex];

            if (value < middleSeqFirstValue) {
                maxSeqRawLeftIndex = middleSeqRawLeftIndex - 2;
            } else {
                int middleSeqLastValue = this.sequences[middleSeqRawLeftIndex + 1];
                if (middleSeqLastValue < value) {
                    minSeqRawLeftIndex = middleSeqRawLeftIndex + 2;
                } else {
                    return middleSeqRawLeftIndex;
                }
            }
        }

        if (minSeqRawLeftIndex != maxSeqRawLeftIndex) { //two different seq's to check
            int minSeqFirstValue = this.sequences[minSeqRawLeftIndex];
            int minSeqLastValue = this.sequences[minSeqRawLeftIndex + 1];

            if (minSeqFirstValue <= value && value <= minSeqLastValue) {
                return minSeqRawLeftIndex;
            } else {
                int maxSeqFirstValue = this.sequences[minSeqRawLeftIndex + 2];
                int maxSeqLastValue = this.sequences[minSeqRawLeftIndex + 3];
                if (maxSeqFirstValue <= value && value <= maxSeqLastValue) {
                    return maxSeqRawLeftIndex;
                } else {
                    return -1;
                }
            }

        } else { // one seq to check
            int minSeqFirstValue = this.sequences[minSeqRawLeftIndex];
            int minSeqLastValue = this.sequences[minSeqRawLeftIndex + 1];

            if (minSeqFirstValue <= value && value <= minSeqLastValue) {
                return minSeqRawLeftIndex;
            } else {
                return -1;
            }
        }
    }

    private int getRawIndexOfSequenceWhichCanBeExtendedOnRight(int value) {
        final int firstSeqLastValue = this.sequences[1];
        if (firstSeqLastValue + 1 == value) {
            return 0;
        } else if (firstSeqLastValue > value) {
            return -1;
        }

        int lastSeqRightIndex = this.reservedIndexes - 1;
        final int lastSeqLastValue = this.sequences[lastSeqRightIndex];

        if (lastSeqLastValue + 1 == value) {
            return lastSeqRightIndex - 1;
        } else if (lastSeqLastValue < value) {
            return -1;
        }

        for (int seqIndex = 1; seqIndex < (this.reservedIndexes / 2) - 1; seqIndex++) {
            int currentSeqLastValue = this.sequences[seqIndex * 2 + 1];

            if (currentSeqLastValue + 1 == value) {
                return seqIndex;
            }
        }

        return -1;
    }

    private int getRawIndexOfSequenceWhichCanBeExtendedOnLeft(int value) {
        final int firstSeqFirstValue = this.sequences[0];
        if (firstSeqFirstValue - 1 == value) {
            return 0;
        } else if (firstSeqFirstValue > value) {
            return -1;
        }

        int lastSeqLeftIndex = this.reservedIndexes - 2;
        final int lastSeqFirstValue = this.sequences[lastSeqLeftIndex];

        if (lastSeqFirstValue - 1 == value) {
            return lastSeqLeftIndex;
        } else if (lastSeqFirstValue < value) {
            return -1;
        }

        for (int seqIndex = 1; seqIndex < (this.reservedIndexes / 2) - 1; seqIndex++) {
            int currentSeqFirstValue = this.sequences[seqIndex * 2];

            if (currentSeqFirstValue - 1 == value) {
                return seqIndex * 2;
            }
        }

        return -1;
    }

    public boolean removeValue(int valToRemove) {
        if (isEmpty()) return false;

        final int rawLeftIndexOfContainerContainingValue = getRawLeftIndexOfSubsequenceContaining(valToRemove);
        if (rawLeftIndexOfContainerContainingValue != -1) {
            final int affectedSeqFirstValue = this.sequences[rawLeftIndexOfContainerContainingValue];
            final int affectedSeqLastValue = this.sequences[rawLeftIndexOfContainerContainingValue + 1];

            if (affectedSeqFirstValue == affectedSeqLastValue) { //delete whole range
                removeSeqByRawLeftIndex(rawLeftIndexOfContainerContainingValue);
            } else if (affectedSeqFirstValue == valToRemove) { //update left value
                this.sequences[rawLeftIndexOfContainerContainingValue] = valToRemove + 1;
            } else if (affectedSeqLastValue == valToRemove) { // update right value
                this.sequences[rawLeftIndexOfContainerContainingValue + 1] = valToRemove - 1;
            } else {// need to divide the subsequence into two parts
                this.sequences[rawLeftIndexOfContainerContainingValue + 1] = valToRemove - 1;
                forceAddNewRange(valToRemove + 1, affectedSeqLastValue);
            }

            return true;
        } else {
            return false;
        }
    }

    public void cutRange(int cutOutFirstValue, int cutOutLastValue) {
        if (isEmpty()) return;

        final int sequencesFirstValue = this.peekFirst();
        final int sequencesLastValue = this.peekLast();

        if (cutOutFirstValue <= sequencesFirstValue && cutOutLastValue >= sequencesLastValue) { //remove all
            this.sequences = new int[0];
            this.reservedIndexes = 0;
        } else { //cutting area is in range (left & right), but is smaller than whole range
            int lastIndexToRemove = getIndexOfLastContainerEndingBefore(cutOutLastValue + 1); //ok

            if (lastIndexToRemove >= 0) {
                int firstIndexToRemove = getIndexOfFirstContainerStartingAfter(cutOutFirstValue - 1); //ok

                if (firstIndexToRemove >= 0 && lastIndexToRemove - firstIndexToRemove >= 0) {
                    for (int i = firstIndexToRemove; i <= lastIndexToRemove; i += 2) { //remove indexes from to (incl.)
                        removeSeqByRawLeftIndex(i);
                    }
                }
            }

            // at this point subsequences fully covered by cutting range are removed

            final int rawLeftIndexOfContainerContainingStartValue = getRawLeftIndexOfSubsequenceContaining(cutOutFirstValue);
            final int rawLeftIndexOfContainerContainingEndValue = getRawLeftIndexOfSubsequenceContaining(cutOutLastValue);

            if (rawLeftIndexOfContainerContainingStartValue >= 0 && rawLeftIndexOfContainerContainingStartValue == rawLeftIndexOfContainerContainingEndValue) { // affected at most one subsequence
                final int firstAffectedSeqFirstValue = this.sequences[rawLeftIndexOfContainerContainingStartValue];
                final int firstAffectedSeqLastValue = this.sequences[rawLeftIndexOfContainerContainingStartValue + 1];

                if (firstAffectedSeqFirstValue < cutOutFirstValue && cutOutLastValue < firstAffectedSeqLastValue) { // need to divide the subsequence into two parts
                    this.sequences[rawLeftIndexOfContainerContainingEndValue + 1] = cutOutFirstValue - 1;
                    forceAddNewRange(cutOutLastValue + 1, firstAffectedSeqLastValue);
                } else if (firstAffectedSeqFirstValue < cutOutFirstValue) { //update right side of affected subsequence
                    this.sequences[rawLeftIndexOfContainerContainingEndValue + 1] = cutOutFirstValue - 1;
                } else { //update left side of affected subsequence
                    this.sequences[rawLeftIndexOfContainerContainingEndValue] = cutOutLastValue + 1;
                }
            } else { //todo merging
                if (rawLeftIndexOfContainerContainingStartValue != -1) {
                    this.sequences[rawLeftIndexOfContainerContainingStartValue + 1] = cutOutFirstValue - 1;
                }

                if (rawLeftIndexOfContainerContainingEndValue != -1) {
                    this.sequences[rawLeftIndexOfContainerContainingEndValue] = cutOutLastValue + 1;
                }
            }
        }
    }

    private int forceAddNewRange(int firstValue, int lastValue) {
        int addedSeqRawLeftIndex = getIndexOfFirstContainerStartingAfter(firstValue);

        if (reservedIndexes == this.sequences.length) { //
            int[] freshArray = new int[((int) (this.sequences.length * (1 + expandFactor)) & (0xfffffffe)) + 2];

            System.arraycopy(this.sequences, 0, freshArray, 0, addedSeqRawLeftIndex); //ok
            System.arraycopy(this.sequences, addedSeqRawLeftIndex, freshArray, addedSeqRawLeftIndex + 2, this.sequences.length - addedSeqRawLeftIndex);

            this.sequences = freshArray;
        } else {
            System.arraycopy(this.sequences, addedSeqRawLeftIndex, this.sequences, addedSeqRawLeftIndex + 2, reservedIndexes - addedSeqRawLeftIndex);
        }

        this.sequences[addedSeqRawLeftIndex] = firstValue; //ok
        this.sequences[addedSeqRawLeftIndex + 1] = lastValue; //ok
        reservedIndexes = reservedIndexes + 2;

        return addedSeqRawLeftIndex;
    }

    private int forceAddNewRangeAtIndex(int firstValue, int lastValue, int newRangeLeftRawIndex) {
        if (reservedIndexes == this.sequences.length) {
            int[] freshArray = new int[((int) (this.sequences.length * (1 + expandFactor)) & (0xfffffffe)) + 2];

            System.arraycopy(this.sequences, 0, freshArray, 0, newRangeLeftRawIndex); //ok
            System.arraycopy(this.sequences, newRangeLeftRawIndex, freshArray, newRangeLeftRawIndex + 2, this.sequences.length - newRangeLeftRawIndex);

            this.sequences = freshArray;
        } else {
            System.arraycopy(this.sequences, newRangeLeftRawIndex, this.sequences, newRangeLeftRawIndex + 2, reservedIndexes - newRangeLeftRawIndex);
        }

        this.sequences[newRangeLeftRawIndex] = firstValue; //ok
        this.sequences[newRangeLeftRawIndex + 1] = lastValue; //ok
        reservedIndexes = reservedIndexes + 2;

        return newRangeLeftRawIndex;
    }

    private int forceAddNewRangeRight(int firstValue, int lastValue) {
        if (reservedIndexes == this.sequences.length) { //
            int[] freshArray = new int[((int) (this.sequences.length * (1 + expandFactor)) & (0xfffffffe)) + 2];

            System.arraycopy(this.sequences, 0, freshArray, 0, this.sequences.length); //ok

            this.sequences = freshArray;
        }

        this.sequences[this.reservedIndexes] = firstValue; //ok
        this.sequences[this.reservedIndexes + 1] = lastValue; //ok
        reservedIndexes = reservedIndexes + 2;

        return reservedIndexes - 2;
    }

    private int forceAddNewRangeLeft(int firstValue, int lastValue) {
        if (reservedIndexes == this.sequences.length) { //
            int[] freshArray = new int[((int) (this.sequences.length * (1 + expandFactor)) & (0xfffffffe)) + 2];

            System.arraycopy(this.sequences, 0, freshArray, 2, this.sequences.length); //ok

            this.sequences = freshArray;
        } else {
            System.arraycopy(this.sequences, 0, this.sequences, 2, this.reservedIndexes);
        }

        this.sequences[0] = firstValue; //ok
        this.sequences[1] = lastValue; //ok
        reservedIndexes = reservedIndexes + 2;

        return 0;
    }

    public int getSequencesCount() {
        return this.reservedIndexes / 2;
    }

    private void removeSeqByRawLeftIndex(int seqRawLeftIndexToRemove) {
        if (seqRawLeftIndexToRemove < this.reservedIndexes - 1 && this.reservedIndexes >= 2) {
            System.arraycopy(this.sequences, seqRawLeftIndexToRemove + 2, this.sequences, seqRawLeftIndexToRemove, this.reservedIndexes - seqRawLeftIndexToRemove - 2);

            this.reservedIndexes -= 2;
        }
    }

    public int pollFirst() {
        if (this.reservedIndexes == 0) {
            throw new IndexOutOfBoundsException();
        } else {
            int firstSeqFirstValue = this.sequences[0];
            int firstSeqLastValue = this.sequences[1];

            if (firstSeqFirstValue == firstSeqLastValue) { //seq must be deleted
                removeSeqByRawLeftIndex(0);
            } else {
                this.sequences[0] = firstSeqFirstValue + 1;
            }

            return firstSeqFirstValue;
        }
    }

    public int pollLast() {
        if (this.reservedIndexes == 0) {
            throw new IndexOutOfBoundsException();
        } else {
            int lastSeqLeftIndex = this.reservedIndexes - 2;
            int lastSeqFirstValue = this.sequences[lastSeqLeftIndex];
            int lastSeqLastValue = this.sequences[lastSeqLeftIndex + 1];

            if (lastSeqFirstValue == lastSeqLastValue) {
                removeSeqByRawLeftIndex(lastSeqLeftIndex);
            } else {
                this.sequences[lastSeqLeftIndex + 1] = lastSeqLastValue - 1;
            }

            return lastSeqLastValue;
        }
    }

    public boolean add(int val) {
        if (!isEmpty()) {
            int lastSeqRawRightIndex = this.reservedIndexes - 1;
            int lastSeqLastValue = this.sequences[lastSeqRawRightIndex];

            if (val >= lastSeqLastValue) {
                int distanceToLastValue = val - lastSeqLastValue;

                if (distanceToLastValue == 1) { //expand last value, no merge is needed
                    this.sequences[lastSeqRawRightIndex] = val;
                    return true;
                } else if (distanceToLastValue > 1) { //add new range, no merge is needed
                    forceAddNewRangeRight(val, val);
                    return true;
                } else {
                    return false;
                }
            } else {
                int firstSeqFirstValue = this.sequences[0];

                if (val <= firstSeqFirstValue) {
                    int distanceToFirstValue = firstSeqFirstValue - val;

                    if (distanceToFirstValue > 1) {
                        forceAddNewRangeLeft(val, val);
                        return true;
                    } else if (distanceToFirstValue == 1) {
                        this.sequences[0] = val;
                        return true;
                    } else {
                        return false;
                    }
                }
            }

            //try add in middle
            int rawLeftIndexOfSeqContainingAddedVal = getIndexOfFirstContainerStartingAfter(val - 1);

            if (rawLeftIndexOfSeqContainingAddedVal == this.reservedIndexes || rawLeftIndexOfSeqContainingAddedVal == 0) {
                return false; //this means, that addValue is present int last or first seq (probably -> to check)
            }

            int firstValueOfProbablyContainingSeq = this.sequences[rawLeftIndexOfSeqContainingAddedVal];
            int lastValueOfSeqBeforeAddedValue = this.sequences[rawLeftIndexOfSeqContainingAddedVal - 1];
            int distanceToRightSeq = firstValueOfProbablyContainingSeq - val;
            int distanceToLeftSeq = val - lastValueOfSeqBeforeAddedValue;

            if (distanceToRightSeq > 1) { //merge with right seq is not needed
                if (distanceToLeftSeq > 1) { //added between two ranges, no merge is needed
                    forceAddNewRangeAtIndex(val, val, rawLeftIndexOfSeqContainingAddedVal);
                    return true;
                } else if (distanceToLeftSeq == 1) { //left seq extended (right), no merge is needed
                    this.sequences[rawLeftIndexOfSeqContainingAddedVal - 1] = val;
                    return true;
                } else { //value is present in left seq
                    return false;
                }
            } else if (distanceToRightSeq == 1) { // distance to right seq == 1
                if (distanceToLeftSeq > 1) { //right can be extended (left), no merge is needed
                    this.sequences[rawLeftIndexOfSeqContainingAddedVal] = val;
                    return true;
                } else if (distanceToLeftSeq == 1) { //new value connects two ranges
                    // Example
                    // Left: [0, 9] Added: (10) Right [11, 19]
                    //
                    // Result :
                    // Left: [0, 19] Right: DELETED

                    this.sequences[rawLeftIndexOfSeqContainingAddedVal - 1] = this.sequences[rawLeftIndexOfSeqContainingAddedVal + 1];
                    removeSeqByRawLeftIndex(rawLeftIndexOfSeqContainingAddedVal);
                    return true;
                } else { //left seq contains added value -> return false
                    return false;
                }

            } else { //distance to right seq == 0, so right seq contains added value, so return false
                return false;
            }

        } else {
            if (this.sequences.length > 0) {
                this.sequences[0] = val;
                this.sequences[1] = val;
            } else {
                this.sequences = new int[]{val, val};
            }
            this.reservedIndexes += 2;
            return true;
        }
    }

    public UniqueIntSequence subSequence(int fromElement, int toElement) {
        if (fromElement > toElement) throw new IllegalArgumentException();
        if (fromElement == toElement) return new UniqueIntSequence(new int[0]);

        int thisFirstValue = peekFirst();
        if (thisFirstValue >= toElement) return new UniqueIntSequence(new int[0]);

        int thisLastValue = peekLast();
        if (thisLastValue < fromElement) return new UniqueIntSequence(new int[0]);

        if (fromElement <= thisFirstValue && thisLastValue < toElement) return new UniqueIntSequence(this);

        int firstResultValue;
        int subSeqContainingFirstResultValueRawLeftIndex;
        if (fromElement <= thisFirstValue) {
            firstResultValue = thisFirstValue;
            subSeqContainingFirstResultValueRawLeftIndex = 0;
        } else {
            subSeqContainingFirstResultValueRawLeftIndex = getRawLeftIndexOfSubsequenceContaining(fromElement);

            if (subSeqContainingFirstResultValueRawLeftIndex != -1) {
                firstResultValue = fromElement;
            } else {
                subSeqContainingFirstResultValueRawLeftIndex = getIndexOfFirstContainerStartingAfter(fromElement);
                firstResultValue = this.sequences[subSeqContainingFirstResultValueRawLeftIndex];
            }
        }

        int lastResultValue;
        int subSeqContainingLastResultValueRawLeftIndex;
        if (toElement > thisLastValue) {
            lastResultValue = thisLastValue;
            subSeqContainingLastResultValueRawLeftIndex = this.reservedIndexes - 2;
        } else {
            subSeqContainingLastResultValueRawLeftIndex = getRawLeftIndexOfSubsequenceContaining(toElement - 1);

            if (subSeqContainingLastResultValueRawLeftIndex != -1) {
                lastResultValue = toElement - 1;
            } else {
                subSeqContainingLastResultValueRawLeftIndex = getIndexOfLastContainerEndingBefore(toElement);
                lastResultValue = this.sequences[subSeqContainingLastResultValueRawLeftIndex + 1];
            }
        }

        if (subSeqContainingFirstResultValueRawLeftIndex > subSeqContainingLastResultValueRawLeftIndex) {
            return new UniqueIntSequence(new int[0]);
        } else if (subSeqContainingFirstResultValueRawLeftIndex == subSeqContainingLastResultValueRawLeftIndex) {
            return new UniqueIntSequence(new int[]{firstResultValue, lastResultValue});
        } else if (subSeqContainingFirstResultValueRawLeftIndex == subSeqContainingLastResultValueRawLeftIndex - 2) {
            final int leftPartLastValue = this.sequences[subSeqContainingFirstResultValueRawLeftIndex + 1];
            final int rightPartFirstValue = this.sequences[subSeqContainingLastResultValueRawLeftIndex];
            return new UniqueIntSequence(new int[]{firstResultValue, leftPartLastValue, rightPartFirstValue, lastResultValue});
        } else {
            int resultArrayLength = subSeqContainingLastResultValueRawLeftIndex - subSeqContainingFirstResultValueRawLeftIndex + 2;
            int[] resultArray = new int[resultArrayLength];
            resultArray[0] = firstResultValue;
            resultArray[resultArrayLength - 1] = lastResultValue;

            int srcPos = subSeqContainingFirstResultValueRawLeftIndex + 1;
            System.arraycopy(this.sequences, srcPos, resultArray, 1, resultArrayLength - 2);

            return new UniqueIntSequence(resultArray);
        }
    }

    public UniqueIntSequence subSequenceToValue(int toElement) {
        int thisFirstValue = peekFirst();
        if (thisFirstValue >= toElement) return new UniqueIntSequence(new int[0]);

        int thisLastValue = peekLast();

        if (thisLastValue < toElement) return new UniqueIntSequence(this);

        int firstResultValue = thisFirstValue;
        int subSeqContainingFirstResultValueRawLeftIndex = 0;

        int lastResultValue;
        int subSeqContainingLastResultValueRawLeftIndex;

        subSeqContainingLastResultValueRawLeftIndex = getRawLeftIndexOfSubsequenceContaining(toElement - 1);

        if (subSeqContainingLastResultValueRawLeftIndex != -1) {
            lastResultValue = toElement - 1;
        } else {
            subSeqContainingLastResultValueRawLeftIndex = getIndexOfLastContainerEndingBefore(toElement);
            lastResultValue = this.sequences[subSeqContainingLastResultValueRawLeftIndex + 1];
        }

        if (subSeqContainingFirstResultValueRawLeftIndex > subSeqContainingLastResultValueRawLeftIndex) {
            return new UniqueIntSequence(new int[0]);
        } else if (subSeqContainingFirstResultValueRawLeftIndex == subSeqContainingLastResultValueRawLeftIndex) {
            return new UniqueIntSequence(new int[]{firstResultValue, lastResultValue});
        } else if (subSeqContainingFirstResultValueRawLeftIndex == subSeqContainingLastResultValueRawLeftIndex - 2) {
            final int leftPartLastValue = this.sequences[subSeqContainingFirstResultValueRawLeftIndex + 1];
            final int rightPartFirstValue = this.sequences[subSeqContainingLastResultValueRawLeftIndex];
            return new UniqueIntSequence(new int[]{firstResultValue, leftPartLastValue, rightPartFirstValue, lastResultValue});
        } else {
            int resultArrayLength = subSeqContainingLastResultValueRawLeftIndex - subSeqContainingFirstResultValueRawLeftIndex + 2;
            int[] resultArray = new int[resultArrayLength];
            resultArray[0] = firstResultValue;
            resultArray[resultArrayLength - 1] = lastResultValue;

            int srcPos = subSeqContainingFirstResultValueRawLeftIndex + 1;
            System.arraycopy(this.sequences, srcPos, resultArray, 1, resultArrayLength - 2);

            return new UniqueIntSequence(resultArray);
        }
    }

    public UniqueIntSequence subSequenceFrom(int fromValue) {
        int thisLastValue = peekLast();
        if (thisLastValue < fromValue) return new UniqueIntSequence(new int[0]);

        int thisFirstValue = peekFirst();
        if (fromValue <= thisFirstValue) return new UniqueIntSequence(this);

        int firstResultValue;
        int subSeqContainingFirstResultValueRawLeftIndex;

        subSeqContainingFirstResultValueRawLeftIndex = getRawLeftIndexOfSubsequenceContaining(fromValue);

        if (subSeqContainingFirstResultValueRawLeftIndex != -1) {
            firstResultValue = fromValue;
        } else {
            subSeqContainingFirstResultValueRawLeftIndex = getIndexOfFirstContainerStartingAfter(fromValue);
            firstResultValue = this.sequences[subSeqContainingFirstResultValueRawLeftIndex];
        }


        int lastResultValue = thisLastValue;
        int subSeqContainingLastResultValueRawLeftIndex = this.reservedIndexes - 2;

        if (subSeqContainingFirstResultValueRawLeftIndex > subSeqContainingLastResultValueRawLeftIndex) {
            return new UniqueIntSequence(new int[0]);
        } else if (subSeqContainingFirstResultValueRawLeftIndex == subSeqContainingLastResultValueRawLeftIndex) {
            return new UniqueIntSequence(new int[]{firstResultValue, lastResultValue});
        } else if (subSeqContainingFirstResultValueRawLeftIndex == subSeqContainingLastResultValueRawLeftIndex - 2) {
            final int leftPartLastValue = this.sequences[subSeqContainingFirstResultValueRawLeftIndex + 1];
            final int rightPartFirstValue = this.sequences[subSeqContainingLastResultValueRawLeftIndex];
            return new UniqueIntSequence(new int[]{firstResultValue, leftPartLastValue, rightPartFirstValue, lastResultValue});
        } else {
            int resultArrayLength = subSeqContainingLastResultValueRawLeftIndex - subSeqContainingFirstResultValueRawLeftIndex + 2;
            int[] resultArray = new int[resultArrayLength];
            resultArray[0] = firstResultValue;
            resultArray[resultArrayLength - 1] = lastResultValue;

            int srcPos = subSeqContainingFirstResultValueRawLeftIndex + 1;
            System.arraycopy(this.sequences, srcPos, resultArray, 1, resultArrayLength - 2);

            return new UniqueIntSequence(resultArray);
        }
    }

    private void tryMergeWithRightNeighbour(int baseContainerRawLeftIndex) { //rawLeftIndex
        if (baseContainerRawLeftIndex < this.reservedIndexes - 2) { //right neighbour exists
            final int rightContainerFirstValue = this.sequences[baseContainerRawLeftIndex + 2];
            final int baseContainerLastValue = this.sequences[baseContainerRawLeftIndex + 1];
            final boolean canBeMergedWithRight = rightContainerFirstValue <= baseContainerLastValue + 1;

            if (canBeMergedWithRight) {
                final int rightContainerLastValue = this.sequences[baseContainerRawLeftIndex + 3];

                this.sequences[baseContainerRawLeftIndex + 1] = rightContainerLastValue; //update base seq
                removeSeqByRawLeftIndex(baseContainerRawLeftIndex + 2); //remove redundant right neighbour

                this.reservedIndexes -= 2;
            }
        }
    }

    private void tryMergeWithNeighbours(int baseContainerRawLeftIndex) { //rawLeftIndex
        if (baseContainerRawLeftIndex >= 2) { //left neighbour exist
            final int baseContainerFirstValue = this.sequences[baseContainerRawLeftIndex];
            final int leftContainerLastValue = this.sequences[baseContainerRawLeftIndex - 1];
            final boolean canBeMergedWithLeft = leftContainerLastValue >= baseContainerFirstValue - 1;

            if (baseContainerRawLeftIndex < this.reservedIndexes - 2) { //both neighbours exist
                final int rightContainerFirstValue = this.sequences[baseContainerRawLeftIndex + 2];
                final int rightContainerLastValue = this.sequences[baseContainerRawLeftIndex + 3];
                final int baseContainerLastValue = this.sequences[baseContainerRawLeftIndex + 1];
                final boolean canBeMergedWithRight = rightContainerFirstValue <= baseContainerLastValue + 1;

                if (canBeMergedWithLeft && canBeMergedWithRight) {
                    final int leftContainerFirstValue = this.sequences[baseContainerRawLeftIndex - 2];
                    this.sequences[baseContainerRawLeftIndex] = leftContainerFirstValue;
                    this.sequences[baseContainerRawLeftIndex + 1] = rightContainerLastValue;
                    removeSeqByRawLeftIndex(baseContainerRawLeftIndex + 2);
                    removeSeqByRawLeftIndex(baseContainerRawLeftIndex - 2);
                } else if (canBeMergedWithLeft) {
                    final int leftContainerFirstValue = this.sequences[baseContainerRawLeftIndex - 2];
                    this.sequences[baseContainerRawLeftIndex] = leftContainerFirstValue;
                    this.sequences[baseContainerRawLeftIndex + 1] = baseContainerLastValue;
                    removeSeqByRawLeftIndex(baseContainerRawLeftIndex - 2);
                } else if (canBeMergedWithRight) {
                    this.sequences[baseContainerRawLeftIndex + 1] = rightContainerLastValue;
                    removeSeqByRawLeftIndex(baseContainerRawLeftIndex + 2);
                }
            } else if (canBeMergedWithLeft) { //only left neighbour exists & can be merged
                final int baseContainerLastValue = this.sequences[baseContainerRawLeftIndex + 1];
                this.sequences[baseContainerRawLeftIndex + 1] = baseContainerLastValue;
                removeSeqByRawLeftIndex(baseContainerRawLeftIndex - 2);
            }
        } else if (baseContainerRawLeftIndex < this.reservedIndexes - 2) { //only right neighbour exists
            final int rightContainerFirstValue = this.sequences[baseContainerRawLeftIndex + 2];
            final int baseContainerLastValue = this.sequences[baseContainerRawLeftIndex + 1];
            final boolean canBeMergedWithRight = rightContainerFirstValue <= baseContainerLastValue + 1;

            if (canBeMergedWithRight) {
                final int rightContainerLastValue = this.sequences[baseContainerRawLeftIndex + 3];
                final int baseContainerFirstValue = this.sequences[baseContainerRawLeftIndex];

                this.sequences[baseContainerRawLeftIndex] = baseContainerFirstValue;
                this.sequences[baseContainerRawLeftIndex + 1] = rightContainerLastValue;
                removeSeqByRawLeftIndex(baseContainerRawLeftIndex + 2);
            }
        }
    }

    public int peekFirst() {
        return this.sequences[0];
    }

    public int peekLast() {
        return this.sequences[this.reservedIndexes - 1];
    }

    public int count() {
        int result = 0;

        if (this.reservedIndexes == 0) return 0;

//        for (int seqIndex = 0; seqIndex < this.reservedIndexes / 2; seqIndex++) {
        int currentRawLeftIndex = 0;
        for (; currentRawLeftIndex <= this.reservedIndexes - 2; currentRawLeftIndex += 2) {
//            int seqSize = this.sequences[(seqIndex * 2) + 1] - this.sequences[(seqIndex * 2)] + 1;
            int seqSize = this.sequences[currentRawLeftIndex + 1] - this.sequences[currentRawLeftIndex] + 1;

            result += seqSize;
        }

        return result;
    }

    public List<Integer> toList() {
        List<Integer> result = new ArrayList<>(this.count());

        for (int seqIndex = 0; seqIndex < this.reservedIndexes / 2; seqIndex++) {
            int sequenceFirstValue = this.sequences[(seqIndex * 2)];
            int sequenceLastValue = this.sequences[(seqIndex * 2) + 1];

            for (int val = sequenceFirstValue; val <= sequenceLastValue; val++) {
                result.add(val);
            }
        }

        return result;
    }

    public int[] toArray() {
        int[] result = new int[this.count()];
        int resultCurrentIndex = 0;

        for (int seqIndex = 0; seqIndex < this.reservedIndexes / 2; seqIndex++) {
            int sequenceFirstValue = this.sequences[(seqIndex * 2)];
            int sequenceLastValue = this.sequences[(seqIndex * 2) + 1];

            for (int val = sequenceFirstValue; val <= sequenceLastValue; val++) {
                result[resultCurrentIndex++] = val;
            }
        }

        return result;
    }

    public int getNthValue(int desiredIndex) { //todo test
        final int count = count();
        if (desiredIndex >= count) throw new OutOfRangeException(desiredIndex, 0, count - 1);

        if (desiredIndex == 0) {
            return peekFirst();
        } else if (desiredIndex == count - 1) {
            return peekLast();
        } else {
            int skipped = 0;
            int distanceToDesiredIndex = desiredIndex;
            int currentSequenceRawLeftIndex = 0;
            int currentSequenceRawRightIndex = 1;
            int currentSequenceFirstValue = this.sequences[currentSequenceRawLeftIndex];
            int currentSequenceLastValue = this.sequences[currentSequenceRawRightIndex];
            int currentSequenceLength = currentSequenceLastValue - currentSequenceFirstValue + 1;

            while (true) {
                if (distanceToDesiredIndex >= currentSequenceLength) {
                    skipped += currentSequenceLength;
                    distanceToDesiredIndex = desiredIndex - skipped;
                    currentSequenceRawLeftIndex += 2;
                    currentSequenceRawRightIndex += 2;
                    currentSequenceFirstValue = this.sequences[currentSequenceRawLeftIndex];
                    currentSequenceLastValue = this.sequences[currentSequenceRawRightIndex];
                    currentSequenceLength = currentSequenceLastValue - currentSequenceFirstValue + 1;
                } else {
                    return currentSequenceFirstValue + distanceToDesiredIndex;
                }
            }
        }

    }

    public boolean isEmpty() {
        return this.reservedIndexes == 0;
    }

    public boolean contains(int c) {
        if (this.reservedIndexes == 0) {
            return false;
        } else {
            int firstVal = peekFirst();

            if (firstVal == c)
                return true;
            else if (firstVal > c)
                return false;

            else {
                int lastVal = peekLast();

                if (c == lastVal)
                    return true;
                else if (c > lastVal)
                    return false;

                else {
                    int minLeftIndex = 0;
                    int maxLeftIndex = this.reservedIndexes - 2;
                    int midLeftIndex;

                    while (maxLeftIndex - minLeftIndex >= 4) {
                        midLeftIndex = ((minLeftIndex + maxLeftIndex) >> 2) << 1;
                        if (this.sequences[midLeftIndex] >= c) {
                            maxLeftIndex = midLeftIndex;
                        } else {
                            minLeftIndex = midLeftIndex;
                        }
                    }

                    for (int leftIndex = minLeftIndex; leftIndex <= maxLeftIndex; leftIndex += 2) {
                        if (this.sequences[leftIndex] <= c && c <= this.sequences[leftIndex + 1]) return true;
                    }

                    return false;
                }
            }
        }
    }

    public void clear() { //todo tests
        this.sequences = new int[0];
    }
}
