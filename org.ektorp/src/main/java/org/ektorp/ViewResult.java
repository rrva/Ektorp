package org.ektorp;

import java.io.*;
import java.util.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.annotate.*;
import org.ektorp.util.*;

/**
 * 
 * @author henrik lundgren
 *
 */
public class ViewResult implements Iterable<ViewResult.Row>, Serializable {

	private static final String OFFSET_FIELD_NAME = "offset";
	private static final String TOTAL_ROWS_FIELD_NAME = "total_rows";
	private static final long serialVersionUID = 4750290767933801714L;
	private int totalRows = -1;
	private int offset = -1;
	private List<Row> rows;
	
	public ViewResult(JsonNode resultNode) {
		Assert.notNull(resultNode, "resultNode may not be null");
		Assert.isTrue(resultNode.findPath("rows").isArray(), "result must contain 'rows' field of array type");
		if (resultNode.get(TOTAL_ROWS_FIELD_NAME) != null) {
			totalRows = resultNode.get(TOTAL_ROWS_FIELD_NAME).getIntValue();
		}
		if (resultNode.get(OFFSET_FIELD_NAME) != null) {
			offset = resultNode.get(OFFSET_FIELD_NAME).getIntValue();
		}
		JsonNode rowsNode = resultNode.get("rows");
		rows = new ArrayList<ViewResult.Row>(rowsNode.size());
		for (JsonNode n : rowsNode) {
			rows.add(new Row(n));
		}
	}
	
	public List<Row> getRows() {
		return rows;
	}
	
	public int getSize() {
		return rows.size();
	}
	/**
	 * 
	 * @return -1 if result did not contain an offset field
	 */
	public int getOffset() {
		return offset;
	}
	
	@JsonProperty
	void setOffset(int offset) {
		this.offset = offset;
	}
	/**
	 * 
	 * @return -1 if result did not contain a total_rows field
	 */
	public int getTotalRows() {
		return totalRows;
	}
	
	@JsonProperty(TOTAL_ROWS_FIELD_NAME)
	void setTotalRows(int i) {
		this.totalRows = i;
	}
	
	public Iterator<ViewResult.Row> iterator() {
		return rows.iterator();
	}
	
	public boolean isEmpty() {
		return rows.isEmpty();
	}
	
	public static class Row {
		
		private static final String VALUE_FIELD_NAME = "value";
		private static final String ID_FIELD_NAME = "id";
		private static final String KEY_FIELD_NAME = "key";
		private static final String DOC_FIELD_NAME = "doc";
		private static final String ERROR_FIELD_NAME = "error";
		private final JsonNode rowNode;
		
		@JsonCreator
		public Row(JsonNode rowNode) {
			Assert.notNull(rowNode, "row node may not be null");
			this.rowNode = rowNode;
			if (getError() != null) {
				throw new ViewResultException(getKeyAsNode(), getError());
			}
		}
		
		public String getId() {
			return rowNode.get(ID_FIELD_NAME).getTextValue();
		}
		
		public String getKey() {
			return nodeAsString(getKeyAsNode());
		}
		
		public JsonNode getKeyAsNode() {
			return rowNode.findPath(KEY_FIELD_NAME);
		}
		
		public String getValue() {
			return nodeAsString(getValueAsNode());
		}
		
		public int getValueAsInt() {
			return getValueAsNode().getValueAsInt(0);
		}
		
		public JsonNode getValueAsNode() {
			return rowNode.findPath(VALUE_FIELD_NAME);
		}
		
		public String getDoc() {
			return nodeAsString(rowNode.findValue(DOC_FIELD_NAME));
		}
		
		public JsonNode getDocAsNode() {
			return rowNode.findPath(DOC_FIELD_NAME);
		}
		
		private String getError() {
			return nodeAsString(rowNode.findValue(ERROR_FIELD_NAME));
		}
		
		private String nodeAsString(JsonNode node) {
			if (isNull(node)) return null;
			return node.isContainerNode() ? node.toString() : node.getValueAsText();
		}

		private boolean isNull(JsonNode node) {
			return node == null || node.isNull() || node.isMissingNode();
		}

	}
	
}
