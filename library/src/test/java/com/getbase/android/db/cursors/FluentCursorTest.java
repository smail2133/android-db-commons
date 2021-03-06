package com.getbase.android.db.cursors;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.database.Cursor;
import android.database.MatrixCursor;

import java.util.NoSuchElementException;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class FluentCursorTest {

  private static final String TEST_COLUMN = "test_column";

  @Test
  public void shouldCloseCursorAfterItIsTransformed() throws Exception {
    final MatrixCursor cursor = new MatrixCursor(new String[] { TEST_COLUMN });
    final FluentCursor fluentCursor = new FluentCursor(cursor);
    fluentCursor.toFluentIterable(new Function<Cursor, Object>() {
      @Override
      public Object apply(Cursor cursor) {
        return null;
      }
    });
    assertThat(fluentCursor.isClosed()).isTrue();
  }

  @Test
  public void shouldApplyGivenFunctionOnEverySingleRow() throws Exception {
    final MatrixCursor cursor = buildMatrixCursor(10);
    final FluentCursor fluentCursor = new FluentCursor(cursor);
    final FluentIterable<Long> transformed = fluentCursor.toFluentIterable(new Function<Cursor, Long>() {
      @Override
      public Long apply(Cursor cursor) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(TEST_COLUMN));
      }
    });
    assertThat(transformed).hasSize(cursor.getCount());
    assertThat(transformed.allMatch(new Predicate<Long>() {
      @Override
      public boolean apply(Long aLong) {
        return aLong.equals(18L);
      }
    })).isTrue();
  }

  @Test
  public void shouldRecognizeNullAsAnEmptyCursor() throws Exception {
    final FluentCursor cursor = new FluentCursor(null);
    assertThat(cursor.getCount()).isZero();
  }

  @Test
  public void shouldBeAbleToCloseQuietlyFluentCursorWrappingNull() throws Exception {
    final FluentCursor cursor = new FluentCursor(null);
    cursor.close();
  }

  @Test
  public void shouldBeAbleToGetColumnIndexFromFluentCursorWrappingNull() throws Exception {
    final FluentCursor cursor = new FluentCursor(null);
    cursor.getColumnIndexOrThrow(TEST_COLUMN);
    cursor.getColumnIndex(TEST_COLUMN);
  }

  @Test
  public void shouldAlwaysCloseCursorAfterCallingToFluentIterable() throws Exception {
    final FluentCursor fluentCursor = new FluentCursor(buildMatrixCursor(10));

    try {
      fluentCursor.toFluentIterable(new Function<Cursor, Object>() {
        @Override
        public Object apply(Cursor input) {
          throw new RuntimeException();
        }
      });
    } catch (Throwable t) {
      // ignore
    }

    assertThat(fluentCursor.isClosed()).isTrue();
  }

  @Test
  public void shouldAcceptFunctionsOperatingOnObject() throws Exception {
    new FluentCursor(null).toFluentIterable(Functions.constant(1L));
  }

  @Test
  public void shouldCloseCursorWhenGettingRowCount() throws Exception {
    Cursor mock = mock(Cursor.class);

    new FluentCursor(mock).toRowCount();

    verify(mock).close();
  }

  @Test
  public void shouldConvertToCorrectRowCount() throws Exception {
    Cursor mock = mock(Cursor.class);
    when(mock.getCount()).thenReturn(42);

    assertThat(new FluentCursor(mock).toRowCount()).isEqualTo(42);
  }

  @Test
  public void shouldCloseCursorAfterTransformingToOnlyElement() throws Exception {
    MatrixCursor cursor = buildMatrixCursor(1);

    new FluentCursor(cursor).toOnlyElement(Functions.constant(null));

    assertThat(cursor).isClosed();
  }

  @Test
  public void shouldCloseCursorAfterTransformingToOnlyElementWithDefaultValue() throws Exception {
    MatrixCursor cursor = buildMatrixCursor(0);

    new FluentCursor(cursor).toOnlyElement(Functions.constant(null), null);

    assertThat(cursor).isClosed();
  }

  @Test(expected = NoSuchElementException.class)
  public void shouldThrowAnExceptionWhenTransformingEmptyCursorToOnlyElement() throws Exception {
    new FluentCursor(buildMatrixCursor(0)).toOnlyElement(Functions.constant(null));
  }

  @Test
  public void shouldReturnDefaultValueWhenTransformingEmptyCursorToOnlyElementWithDefaultValue() throws Exception {
    Integer shouldBeDefault = new FluentCursor(buildMatrixCursor(0)).toOnlyElement(Functions.constant(1500), 2900);

    assertThat(shouldBeDefault).isEqualTo(2900);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowAnExceptionWhenTransformingCursorWithMultipleToOnlyElement() throws Exception {
    new FluentCursor(buildMatrixCursor(10)).toOnlyElement(Functions.constant(null));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowAnExceptionWhenTransformingCursorWithMultipleToOnlyElementWithDefaultValue() throws Exception {
    new FluentCursor(buildMatrixCursor(10)).toOnlyElement(Functions.constant(null), null);
  }

  @Test
  public void shouldApplyGivenFunctionWhenTransformingCursorToOnlyElement() throws Exception {
    MatrixCursor cursor = buildMatrixCursor(1);

    Integer onlyElement = new FluentCursor(cursor).toOnlyElement(Functions.constant(1500));

    assertThat(onlyElement).isEqualTo(1500);
  }

  @Test
  public void shouldApplyGivenFunctionWhenTransformingCursorToOnlyElementWithDefaultValue() throws Exception {
    MatrixCursor cursor = buildMatrixCursor(1);

    Integer onlyElement = new FluentCursor(cursor).toOnlyElement(Functions.constant(1500), 2900);

    assertThat(onlyElement).isEqualTo(1500);
  }

  @Test
  public void shouldNotIterateOverCursorWhenTransformingCursorToOnlyElement() throws Exception {
    Cursor cursor = spy(buildMatrixCursor(1));

    new FluentCursor(cursor).toOnlyElement(Functions.constant(null));

    verify(cursor, never()).moveToNext();
    verify(cursor, never()).moveToLast();
    verify(cursor, never()).moveToPrevious();
    verify(cursor, never()).moveToPosition(anyInt());
  }

  @Test
  public void shouldNotIterateOverCursorWhenTransformingCursorToOnlyElementWithDefaultValue() throws Exception {
    Cursor cursor = spy(buildMatrixCursor(0));

    new FluentCursor(cursor).toOnlyElement(Functions.constant(null), null);

    verify(cursor, never()).moveToNext();
    verify(cursor, never()).moveToLast();
    verify(cursor, never()).moveToPrevious();
    verify(cursor, never()).moveToPosition(anyInt());
  }

  @Test
  public void shouldNotIterateOverCursorWhenTransformingCursorToRowCount() throws Exception {
    Cursor mock = mock(Cursor.class);

    new FluentCursor(mock).toRowCount();

    verify(mock, never()).moveToFirst();
    verify(mock, never()).moveToNext();
    verify(mock, never()).moveToLast();
    verify(mock, never()).moveToPrevious();
    verify(mock, never()).moveToPosition(anyInt());
  }

  private MatrixCursor buildMatrixCursor(int count) {
    final MatrixCursor cursor = new MatrixCursor(new String[] { TEST_COLUMN });
    for (int i = 0; i < count; i++) {
      cursor.addRow(new Object[] { 18L });
    }
    return cursor;
  }
}
