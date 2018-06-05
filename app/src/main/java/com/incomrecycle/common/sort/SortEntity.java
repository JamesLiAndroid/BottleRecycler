package com.incomrecycle.common.sort;

public abstract class SortEntity {
    private Object m_SortedObj = null;

    public enum COMPARE {
        LITTLE,
        EQUAL,
        GREATE
    }

    public enum ORDERBY {
        ASC,
        DESC
    }

    protected abstract COMPARE compare(int i, int i2);

    protected abstract void exchange(int i, int i2);

    protected abstract int length();

    public SortEntity(Object tSortedObj) {
        this.m_SortedObj = tSortedObj;
    }

    protected Object getObject() {
        return this.m_SortedObj;
    }

    private void internalSort(int iStart, int iEnd) {
        int iLow = iStart;
        int iHigh = iEnd;
        if (iLow < iHigh) {
            int iMid = (iLow + iHigh) / 2;
            while (iLow < iHigh) {
                while (iLow < iHigh && compare(iLow, iMid) == COMPARE.LITTLE) {
                    iLow++;
                }
                while (iLow < iHigh && compare(iHigh, iMid) == COMPARE.GREATE) {
                    iHigh--;
                }
                if (iLow < iHigh) {
                    exchange(iLow, iHigh);
                    iLow++;
                    iHigh--;
                }
            }
            if (iHigh < iLow) {
                int iTemp = iHigh;
                iHigh = iLow;
                iLow = iTemp;
            }
            internalSort(iStart, iLow);
            internalSort(iLow == iStart ? iLow + 1 : iLow, iEnd);
        }
    }

    public void sort() {
        if (length() > 1) {
            while (true) {
                boolean isSorted = true;
                for (int s = 1; s < length(); s++) {
                    if (compare(s - 1, s) == COMPARE.GREATE) {
                        isSorted = false;
                        break;
                    }
                }
                if (!isSorted) {
                    internalSort(0, length() - 1);
                } else {
                    return;
                }
            }
        }
    }
}
