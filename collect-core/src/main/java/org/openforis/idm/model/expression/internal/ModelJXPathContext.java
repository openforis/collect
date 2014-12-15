/**
 *
 */
package org.openforis.idm.model.expression.internal;

import org.apache.commons.jxpath.CompiledExpression;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.Parser;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyObject;
import org.openforis.idm.metamodel.validation.LookupProvider;
import org.openforis.idm.model.Node;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author M. Togna
 * @author G. Miceli
 * @author S. Ricci
 */
@SuppressWarnings("rawtypes")
public class ModelJXPathContext extends JXPathContextReferenceImpl {

	private static int cleanupCount = 0;
	// The frequency of the cache cleanup
	private static final int CLEANUP_THRESHOLD = 500;
	private LookupProvider lookupProvider;
	private final Map<String, Object> compiled;
	private boolean normalizeNumbers;
	private static volatile ModelJXPathContext compilationContext;
	private ReferencedPathEvaluator referencedPathEvaluator;
	private Survey survey;

	protected ModelJXPathContext(JXPathContext parentContext, Object contextNode) {
		super(parentContext, contextNode);
		this.compiled = new HashMap<String, Object>();
		this.normalizeNumbers = false;
		if (contextNode instanceof Node<?>) {
			this.survey = ((Node) contextNode).getSurvey();
		} else if (contextNode instanceof SurveyObject) {
			this.survey = ((SurveyObject) contextNode).getSurvey();
		}
	}

	/**
	 * Compiles the supplied XPath and returns an internal representation
	 * of the path that can then be evaluated.  Use CompiledExpressions
	 * when you need to evaluate the same expression multiple times
	 * and there is a convenient place to cache CompiledExpression
	 * between invocations.
	 *
	 * @param xpath to compile
	 * @return CompiledExpression
	 */
	public synchronized static CompiledExpression compile(ReferencedPathEvaluator referencedPathEvaluator, String xpath, boolean normalizeNumbers) {
		if (compilationContext == null) {
			compilationContext = (ModelJXPathContext) JXPathContext.newContext(null);
		}
		compilationContext.setNormalizeNumbers(normalizeNumbers);
		compilationContext.referencedPathEvaluator = referencedPathEvaluator;
		return compilationContext.compilePath(xpath);
	}

	public static ModelJXPathContext newContext(JXPathContext parentContext, Object contextNode) {
		ModelJXPathContext jxPathContext = new ModelJXPathContext(parentContext, contextNode);
		copyProperties(parentContext, jxPathContext);
		return jxPathContext;
	}

	private static void copyProperties(JXPathContext fromContext, ModelJXPathContext toContext) {
		if (!(fromContext == null || toContext == null)) {
			if (fromContext instanceof ModelJXPathContext) {
				toContext.setLookupProvider(((ModelJXPathContext) fromContext).getLookupProvider());
			}
		}
	}

	@Override
	protected ModelJXPathCompiledExpression compilePath(String xpath) {
		Expression expr = compileExpression(xpath);
		ModelJXPathCompiledExpression compiledExpression = new ModelJXPathCompiledExpression(referencedPathEvaluator, xpath, expr);
		return compiledExpression;
	}

	@Override
	protected Compiler getCompiler() {
		ModelTreeCompiler treeCompiler = new ModelTreeCompiler();
		if (normalizeNumbers) {
			treeCompiler.setNormalizeNumbers(normalizeNumbers);
		}
		return treeCompiler;
	}

	public LookupProvider getLookupProvider() {
		return lookupProvider;
	}

	public void setLookupProvider(LookupProvider lookupProvider) {
		this.lookupProvider = lookupProvider;
	}

	public void setNormalizeNumbers(boolean normalizeNumbers) {
		this.normalizeNumbers = normalizeNumbers;
	}

	public Survey getSurvey() {
		return survey;
	}

	@SuppressWarnings("unchecked")
	private Expression compileExpression(String xpath) {
		Expression expr;

		synchronized (compiled) {
			if (USE_SOFT_CACHE) {
				expr = null;
				SoftReference ref = (SoftReference) compiled.get(xpath);
				if (ref != null) {
					expr = (Expression) ref.get();
				}
			} else {
				expr = (Expression) compiled.get(xpath);
			}
		}

		if (expr != null) {
			return expr;
		}

		expr = (Expression) Parser.parseExpression(xpath, getCompiler());

		synchronized (compiled) {
			if (USE_SOFT_CACHE) {
				if (cleanupCount++ >= CLEANUP_THRESHOLD) {
					Iterator it = compiled.entrySet().iterator();
					while (it.hasNext()) {
						Entry me = (Entry) it.next();
						if (((SoftReference) me.getValue()).get() == null) {
							it.remove();
						}
					}
					cleanupCount = 0;
				}
				compiled.put(xpath, new SoftReference(expr));
			} else {
				compiled.put(xpath, expr);
			}
		}

		return expr;
	}

}
