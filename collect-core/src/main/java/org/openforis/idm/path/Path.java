package org.openforis.idm.path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;

/**
 * 
 * @author G. Miceli
 *
 */
public final class Path implements Axis, Iterable<PathElement> {

	public static final char SEPARATOR = '/';
	public static final String SEPARATOR_REGEX = "\\/";
	public static final String THIS_FUNCTION = "this()";
	public static final String THIS_SYMBOL = ".";
	public static final String THIS_VARIABLE = "$this";
	public static final Set<String> THIS_ALIASES = new HashSet<String>(Arrays.asList(
													THIS_FUNCTION, 
													THIS_SYMBOL,
													THIS_VARIABLE));
	public static final String CONTEXT_VARIABLE = "$context";
	public static final String PARENT_FUNCTION = "parent()";
	public static final String PARENT_SYMBOL = "..";
	public static final String NORMALIZED_PARENT_FUNCTION = "__parent";
	public static final Set<String> PARENT_ALIASES = new HashSet<String>(Arrays.asList(
													PARENT_FUNCTION, 
													PARENT_SYMBOL, 
													NORMALIZED_PARENT_FUNCTION));
	private static final String PARENT_FUNCTION_PATTERN = "\\bparent\\(\\)";
	
	private Path parentPath;
	private PathElement lastElement;
	private boolean absolute;
	
	private Path(Path parentPath, PathElement lastElement) {
		this.parentPath = parentPath;
		this.lastElement = lastElement;
		this.absolute = parentPath.absolute;
	}

	private Path(PathElement firstElement, boolean absolute) {
		this.lastElement = firstElement;
		this.absolute = absolute;
	}
	
	public Path getParentPath() {
		return parentPath;
	}
	
	public Axis getAxis() {
		return lastElement;
	}

	public Path append(PathElement lastElement) {
		return new Path(this, lastElement);
	}

	public Path appendElement(String name) {
		return new Path(this, new PathElement(name));
	}

	public Path appendElement(String name, int idx) {
		return new Path(this, new PathElement(name, idx));
	}
	
	public boolean isAbsolute() {
		return absolute;
	}
	
	@Override
	public List<Node<?>> evaluate(Node<?> context) {
		if ( absolute ) {
			Record record = context.getRecord();
			return evaluate(record);
		} else if ( parentPath == null ) {
			List<Node<?>> res = lastElement.evaluate(context);
			if ( res == null ) {
				throw new InvalidPathException(toString());
			}
			return res;
		} else {
			List<Node<?>> contexts = parentPath.evaluate(context);
			List<Node<?>> results = new ArrayList<Node<?>>();
			for (Node<?> ctx : contexts) {
				List<Node<?>> eval = lastElement.evaluate(ctx);
				if ( eval == null ) {
					throw new InvalidPathException(toString());
				}
				results.addAll(eval);
			}
			return Collections.unmodifiableList(results);
		}
	}

	@Override
	public List<Node<?>> evaluate(Record context) {
		if ( parentPath == null ) {
			return lastElement.evaluate(context);
		} else {
			List<Node<?>> contexts = parentPath.evaluate(context);
			List<Node<?>> results = new ArrayList<Node<?>>();
			for (Node<?> ctx : contexts) {
				List<Node<?>> eval = lastElement.evaluate(ctx);
				results.addAll(eval);
			}
			return Collections.unmodifiableList(results);
		}
	}
	
	@Override
	public NodeDefinition evaluate(NodeDefinition context) {
		if ( absolute ) {
			Schema schema = context.getSchema();
			return evaluate(schema);
		} else { 
			if ( parentPath != null ) {
				context = parentPath.evaluate(context);
			}
			return lastElement.evaluate(context);
		}
	}

	@Override
	public NodeDefinition evaluate(Schema context) {
		if ( parentPath == null ) {
			return lastElement.evaluate(context);
		} else {
			NodeDefinition ctx = parentPath.evaluate(context);
			return lastElement.evaluate(ctx);
		}
	}
	
	public static Path parse(String path) throws InvalidPathException {
		int idx = path.lastIndexOf('/');
		if ( idx < 0 ) {
			PathElement lastElement = PathElement.parseElement(path);
			return new Path(lastElement, false);
		} else {
			String head = path.substring(0, idx);
			String tail = path.substring(idx+1);
			PathElement lastElement = PathElement.parseElement(tail);
			if ( idx == 0 ){
				return new Path(lastElement, true);
			} else {
				Path parentPath = parse(head);
				return parentPath.append(lastElement);
			}
		}
	}

	public static String getAbsolutePath(String path) {
		return PathUtils.absolutePath(path);
	}
	
	public static String getRelativePath(String source, String dest) {
		return PathUtils.relativePath(source, dest);
	}

	private static void append(StringBuilder sb, String value,
			char separator, int count) {
		for (int i = 0; i < count; i++) {
			sb.append(value);
			if (i < count - 1) {
				sb.append(separator);
			}
		}
	}

	private static int countOccurrences(String source, char value) {
		int count = 0;
		for (int i = 0; i < source.length(); i++) {
			if (source.charAt(i) == value) {
				count++;
			}
		}
		return count;
	}

	public static String removeThisVariableToken(String path) {
		return path.replaceAll(Pattern.quote(THIS_VARIABLE) + SEPARATOR, "");
	}
	
	public static String getNormalizedPath(String path) {
		return path.replaceAll(PARENT_FUNCTION_PATTERN, NORMALIZED_PARENT_FUNCTION);
	}

	public static Path pathOf(NodeDefinition defn) {
		NodeDefinition parent = defn.getParentDefinition();
		String name = defn.getName();
		PathElement elem = new PathElement(name);
		if ( parent == null ) {
			return new Path(elem, true);
		} else {
			Path parentPath = Path.pathOf(parent);
			return parentPath.append(elem);
		}
	}

	public static Path relative(String... elements) {
		return fromElements(false, elements);
	}

	public static Path absolute(String... elements) {
		return fromElements(true, elements);
	}

	private static Path fromElements (boolean absolute, String... elements) {
		Path path = null;
		for (String element : elements) {
			PathElement e = new PathElement(element);
			if ( path == null ) {
				path = new Path(e, absolute);
			} else {
				path = path.append(e);
			}
		}
		return path;
	}
	
	public List<PathElement> elements() {
		List<PathElement> list = new ArrayList<PathElement>();
		elements(list);
		return Collections.unmodifiableList(list);
	}
	
	private void elements(List<PathElement> list) {
		if ( parentPath != null ) {
			parentPath.elements(list);
		}
		list.add(lastElement);
	}

	@Override
	public Iterator<PathElement> iterator() {
		return elements().iterator();
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		toString(sb);
		return sb.toString();
	}

	private void toString(StringBuffer sb) {
		if ( absolute ) {
			sb.append('/');
		}
		if ( parentPath != null ) {
			parentPath.toString(sb);
			sb.append('/');
		}
		sb.append(lastElement);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lastElement == null) ? 0 : lastElement.hashCode());
		result = prime * result + ((parentPath == null) ? 0 : parentPath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Path other = (Path) obj;
		if (lastElement == null) {
			if (other.lastElement != null)
				return false;
		} else if (!lastElement.equals(other.lastElement))
			return false;
		if (parentPath == null) {
			if (other.parentPath != null)
				return false;
		} else if (!parentPath.equals(other.parentPath))
			return false;
		return true;
	}
	
	private static class PathUtils {
		
		private static final String RELATIVE_PATH_CONTENT_REGEX = "\\[[^\\[]+\\]";

		static String absolutePath(String path) {
			return path.replaceAll(RELATIVE_PATH_CONTENT_REGEX, "");
		}
		
		static String relativePath(String source, String dest) {
			if (source.equals(dest)) {
				return THIS_FUNCTION;
			}
			String commonPath = calculateCommonPath(source, dest);

			int sourcePathElementsCount = countOccurrences(source, SEPARATOR);
			int commonPathElementsCount = countOccurrences(commonPath, SEPARATOR);
			
			StringBuilder pathBuilder = new StringBuilder();
			
			int innerSourceLevels = sourcePathElementsCount - commonPathElementsCount;
			if (innerSourceLevels > 0) {
				append(pathBuilder, PARENT_FUNCTION, SEPARATOR, innerSourceLevels);
			}
			int destLastPartIdx = commonPath.length() + 1;
			if (destLastPartIdx < dest.length()) {
				if (pathBuilder.length() > 0) {
					pathBuilder.append(SEPARATOR);
				}
				pathBuilder.append(dest.substring(destLastPartIdx));
			}

			if ( pathBuilder.length() == 0 ) {
				return THIS_FUNCTION;
			} else {
				return pathBuilder.toString();
			}
		}

		static String calculateCommonPath(String source, String dest) {
			String commonPrefix = longestCommonPrefix(source, dest);
			if (source.length() > commonPrefix.length() && source.charAt(commonPrefix.length()) != SEPARATOR || 
					dest.length() > commonPrefix.length() && dest.charAt(commonPrefix.length()) != SEPARATOR) {
				return commonPrefix.substring(0, commonPrefix.lastIndexOf(SEPARATOR));
			} else {
				return commonPrefix;
			}
		}

		static String longestCommonPrefix(String... values) {
			if (values == null || values.length == 0) {
				return "";
			}
		    int minLength = Integer.MAX_VALUE;
		    for(String str: values){
		        if(minLength > str.length()) {
		            minLength = str.length();
		        }
		    }
		    if(minLength == 0) {
		    	return "";
		    }
			for (int j = 0; j < minLength; j++) {
				char prev = '0';
				for (int valueIndex = 0; valueIndex < values.length; valueIndex++) {
					if (valueIndex == 0) {
						prev = values[valueIndex].charAt(j);
						continue;
					}
					if (values[valueIndex].charAt(j) != prev) {
						return values[valueIndex].substring(0, j);
					}
				}
			}
			return values[0].substring(0, minLength);
		}
	}
	
}
