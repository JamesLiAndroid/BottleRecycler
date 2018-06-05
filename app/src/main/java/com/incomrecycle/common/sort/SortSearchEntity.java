package com.incomrecycle.common.sort;

public abstract class SortSearchEntity {
    private Object m_SortedObj = null;
    private ORDERBY orderBy;

    public enum COMPARE {
        LITTLE,
        EQUAL,
        GREATE
    }

    public enum ORDERBY {
        ASC,
        DESC
    }

    protected abstract COMPARE compare(Object obj, int i);

    protected abstract int length();

    public SortSearchEntity(Object tSortedObj, ORDERBY orderBy) {
        this.m_SortedObj = tSortedObj;
        this.orderBy = orderBy;
    }

    protected Object getObject() {
        return this.m_SortedObj;
    }

    public int find(Object obj) {
        return binaryFind(obj, 0, length() - 1);
    }

    private int binaryFind(Object obj, int fromIndex, int toIndex) {
        if (fromIndex > toIndex) {
            return fromIndex;
        }
        int mid = (fromIndex + toIndex) / 2;
        if (internalCompare(obj, mid) == COMPARE.GREATE) {
            return binaryFind(obj, mid + 1, toIndex);
        }
        return internalCompare(obj, mid) == COMPARE.LITTLE ? binaryFind(obj, fromIndex, mid - 1) : mid;
    }

    private COMPARE internalCompare(Object obj, int idx) {
        COMPARE r = compare(obj, idx);
        if (this.orderBy == ORDERBY.ASC || r == COMPARE.EQUAL) {
            return r;
        }
        if (r == COMPARE.LITTLE) {
            return COMPARE.GREATE;
        }
        return COMPARE.LITTLE;
    }
}
