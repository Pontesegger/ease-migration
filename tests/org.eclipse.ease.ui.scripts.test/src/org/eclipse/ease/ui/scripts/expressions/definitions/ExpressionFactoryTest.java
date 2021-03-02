package org.eclipse.ease.ui.scripts.expressions.definitions;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.expressions.Expression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ExpressionFactoryTest {

	private List<Expression> fChildExpressions;

	@BeforeEach
	public void setup() {
		final Expression expression = ExpressionFactory.getInstance().createWithExpression("dummy");

		fChildExpressions = new ArrayList<>();
		fChildExpressions.add(expression);
	}

	@Test
	@DisplayName("Load class AndExpression")
	public void loadClassAndExpression() {
		final Class<?> clazz = ExpressionFactory.loadClazz("AndExpression");
		assertNotNull(clazz);
	}

	@Test
	@DisplayName("Load class OrExpression")
	public void loadClassOrExpression() {
		final Class<?> clazz = ExpressionFactory.loadClazz("OrExpression");
		assertNotNull(clazz);
	}

	@Test
	@DisplayName("Load class WithExpression")
	public void loadClassWithExpression() {
		final Class<?> clazz = ExpressionFactory.loadClazz("WithExpression");
		assertNotNull(clazz);
	}

	@Test
	@DisplayName("Load class ReferenceExpression")
	public void loadClassReferenceExpression() {
		final Class<?> clazz = ExpressionFactory.loadClazz("ReferenceExpression");
		assertNotNull(clazz);
	}

	@Test
	@DisplayName("Load class EqualsExpression")
	public void loadClassEqualsExpression() {
		final Class<?> clazz = ExpressionFactory.loadClazz("EqualsExpression");
		assertNotNull(clazz);
	}

	@Test
	@DisplayName("Load class CountExpression")
	public void loadClassCountExpression() {
		final Class<?> clazz = ExpressionFactory.loadClazz("CountExpression");
		assertNotNull(clazz);
	}

	@Test
	@DisplayName("Load class TestExpression")
	public void loadClassTestExpression() {
		final Class<?> clazz = ExpressionFactory.loadClazz("TestExpression");
		assertNotNull(clazz);
	}

	@Test
	@DisplayName("Create AndExpression")
	public void createAndExpression() {
		final Expression expression = ExpressionFactory.getInstance().createAndExpression(fChildExpressions);
		assertNotNull(expression);
	}

	@Test
	@DisplayName("Create OrExpression")
	public void createOrExpression() {
		final Expression expression = ExpressionFactory.getInstance().createOrExpression(fChildExpressions);
		assertNotNull(expression);
	}

	@Test
	@DisplayName("Create WithExpression")
	public void createWithExpression() {
		final Expression expression = ExpressionFactory.getInstance().createWithExpression("something");
		assertNotNull(expression);
	}

	@Test
	@DisplayName("Create ReferenceExpression")
	public void createReferenceExpression() {
		final Expression expression = ExpressionFactory.getInstance().createReferenceExpression("something");
		assertNotNull(expression);
	}

	@Test
	@DisplayName("Create EqualsExpression")
	public void createEqualsExpression() {
		final Expression expression = ExpressionFactory.getInstance().createEqualsExpression("something");
		assertNotNull(expression);
	}

	@Test
	@DisplayName("Create CountExpression")
	public void createCountExpression() {
		final Expression expression = ExpressionFactory.getInstance().createCountExpression("something");
		assertNotNull(expression);
	}

	@Test
	@DisplayName("Create TestExpression")
	public void createTestExpression() {
		final Expression expression = ExpressionFactory.getInstance().createTestExpression("namespace", "property", new Object[0], new Object(), false);
		assertNotNull(expression);
	}
}
