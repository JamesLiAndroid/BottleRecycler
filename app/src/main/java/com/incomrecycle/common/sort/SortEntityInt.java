package com.incomrecycle.common.sort;

public abstract class SortEntityInt extends SortEntity {
    private ORDERBY m_orderby = ORDERBY.ASC;

    protected abstract long getIntVal(int i);

    public SortEntityInt(Object tSortedObj, ORDERBY orderby) {
        super(tSortedObj);
        this.m_orderby = orderby;
    }

    protected final COMPARE compare(int iFirst, int iSecond) {
        if (this.m_orderby == ORDERBY.ASC) {
            return internalCompare(iFirst, iSecond);
        }
        return internalCompare(iSecond, iFirst);
    }

    private COMPARE internalCompare(int iFirst, int iSecond) {
        long lFirstVal = getIntVal(iFirst);
        long lSecondVal = getIntVal(iSecond);
        if (lFirstVal == lSecondVal) {
            return COMPARE.EQUAL;
        }
        if (lFirstVal > lSecondVal) {
            return COMPARE.GREATE;
        }
        return COMPARE.LITTLE;
    }
}
