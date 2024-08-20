package com.money.manager.ex.nestedcategory;

import com.money.manager.ex.Constants;
import com.money.manager.ex.domainmodel.Category;
import com.money.manager.ex.domainmodel.EntityBase;

public class NestedCategoryEntity
        extends EntityBase {
    public static int ACTIVE = 1;
    public static int NOT_ACTIVE = 0;

    public NestedCategoryEntity(Category category) {
        super();
        setMandatoryField(category.getId(), category.getName(), category.getParentId());
    }

    public NestedCategoryEntity() {
        super();
        setCategoryId(Constants.NOT_SET);
        setCategoryName(Constants.EMPTY_STRING);
        setParentId(Constants.NOT_SET);
        setActive(Constants.NOT_SET);
    }

    public NestedCategoryEntity(int categoryId, String categoryName, int parentId) {
        super();
        setMandatoryField(categoryId, categoryName, parentId);
    }

    public int getCategoryId() {
        return getInt(QueryNestedCategory.CATEGID);
    }

    public void setCategoryId(int id) {
        setInt(QueryNestedCategory.CATEGID, id);
    }

    public String getCategoryName() {
        return getString(QueryNestedCategory.CATEGNAME);
    }

    public void setCategoryName(String name) {
        setString(QueryNestedCategory.CATEGNAME, name);
    }

    public int getParentId() {
        return getInt(QueryNestedCategory.PARENTID);
    }

    public void setParentId(int id) {
        setInt(QueryNestedCategory.PARENTID, id);
    }

    public String getParentName() {
        return getString(QueryNestedCategory.PARENTNAME);
    }

    public void setParentName(String name) {
        setString(QueryNestedCategory.PARENTNAME, name);
    }

    public String getBasename() {
        return getString(QueryNestedCategory.BASENAME);
    }

    public void setBasename(String basename) {
        setString(QueryNestedCategory.BASENAME, basename);
    }

    public int getActive() {
        return getInt(QueryNestedCategory.ACTIVE);
    }

    public void setActive(int active) {
        setInt(QueryNestedCategory.ACTIVE, active);
    }

    public int getLevel() {
        return getInt(QueryNestedCategory.LEVEL);
    }

    public void setLevel(int level) {
        setInt(QueryNestedCategory.LEVEL, level);
    }

    public void setMandatoryField(int categoryId, String categoryName, int parentId) {
        setCategoryId(categoryId);
        setCategoryName(categoryName);
        setParentId(parentId);
        setActive(ACTIVE);
    }

    public boolean hasParent() {
        return this.getParentId() != Constants.NOT_SET;
    }

    public Category asCategory() {
        Category category = new Category();
        category.setId(getCategoryId());
        category.setName(getCategoryName());
        category.setParentId(getParentId());
        category.setBasename(getBasename());
        return category;
    }

}
