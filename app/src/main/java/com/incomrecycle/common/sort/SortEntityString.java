package com.incomrecycle.common.sort;

public abstract class SortEntityString extends SortEntity {
    private ORDERBY m_orderby = ORDERBY.ASC;

    protected abstract String getStringVal(int i);

    public SortEntityString(Object tSortedObj, ORDERBY orderby) {
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
        String sFirstVal = getStringVal(iFirst);
        String sSecondVal = getStringVal(iSecond);
        if (sFirstVal == sSecondVal) {
            return COMPARE.EQUAL;
        }
        if (sFirstVal == null) {
            return COMPARE.LITTLE;
        }
        if (sSecondVal == null) {
            return COMPARE.GREATE;
        }
        int iCompare = sFirstVal.compareTo(sSecondVal);
        if (iCompare > 0) {
            return COMPARE.GREATE;
        }
        if (iCompare < 0) {
            return COMPARE.LITTLE;
        }
        return COMPARE.EQUAL;
    }
}
