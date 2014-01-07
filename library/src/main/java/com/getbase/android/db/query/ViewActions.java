package com.getbase.android.db.query;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import android.database.sqlite.SQLiteDatabase;

public final class ViewActions {
  private ViewActions() {
  }

  public static ViewSelector<ViewSelectStatementChooser> create() {
    return new CreateViewAction();
  }

  public static ViewSelector<ViewAction> dropIfExists() {
    return new DropViewAction();
  }

  public static class DropViewAction implements ViewSelector<ViewAction>, ViewAction {
    private String mView;

    DropViewAction() {
    }

    @Override
    public void perform(SQLiteDatabase db) {
      db.execSQL("DROP VIEW IF EXISTS " + mView);
    }

    @Override
    public ViewAction view(String view) {
      mView = checkNotNull(view);
      return this;
    }
  }

  public static class CreateViewAction implements ViewSelector<ViewSelectStatementChooser>, ViewAction, ViewSelectStatementChooser {
    private String mView;
    private Query mQuery;

    CreateViewAction() {
    }

    @Override
    public void perform(SQLiteDatabase db) {
      db.execSQL("CREATE VIEW " + mView + " AS " + mQuery.mRawQuery);
    }

    @Override
    public ViewAction as(Query query) {
      mQuery = checkNotNull(query);

      checkArgument(query.mRawQueryArgs.isEmpty(), "Cannot use query with bound args for View creation");

      return this;
    }

    @Override
    public ViewSelectStatementChooser view(String view) {
      mView = checkNotNull(view);
      return this;
    }
  }

  public interface ViewSelector<T> {
    T view(String view);
  }

  public interface ViewAction {
    void perform(SQLiteDatabase db);
  }

  public interface ViewSelectStatementChooser {
    ViewAction as(Query query);
  }
}
