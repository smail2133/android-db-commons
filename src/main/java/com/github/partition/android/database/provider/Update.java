package com.github.partition.android.database.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

public class Update extends ProviderAction<Integer> {

  private final Selection selection = new Selection();
  private ContentValues values;

  Update(Uri uri) {
    super(uri);
  }

  public Update values(ContentValues values) {
    this.values = values;
    return this;
  }

  public Update where(String selection, String... selectionArgs) {
    this.selection.append(selection, selectionArgs);
    return this;
  }

  @Override
  public Integer perform(ContentResolver contentResolver) {
    return contentResolver.update(getUri(), values, selection.getSelection(), selection.getSelectionArgs());
  }
}
