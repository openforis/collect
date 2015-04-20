package org.openforis.idm.model;

import java.io.IOException;
import java.util.List;

import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;

import com.dyuproject.protostuff.Input;
import com.dyuproject.protostuff.Output;
import com.dyuproject.protostuff.ProtostuffException;

/**
 * @author G. Miceli
 * @author S. Ricci
 * 
 */
@SuppressWarnings("unchecked")
public class EntitySchema extends SchemaSupport<Entity> {

	private static final int DEFINITION_ID_FIELD_NUMBER = 1;
	private static final int NODE_FIELD_NUMBER = 2;
	private static final int CHILD_NODE_STATE_FIELD_NUMBER = 3;
	private static final int CHILD_DEFINITION_ID_FIELD_NUMBER = 4;

	public EntitySchema() {
		super(Entity.class, "children", "childStates");
	}

	@Override
	public String messageName() {
		return "entity";
	}

	@Override
	public void writeTo(Output out, Entity entity) throws IOException {
		List<Node<? extends NodeDefinition>> children = entity.getChildren();
        for(Node<?> node : children) {
        	if(isNodeToBeSaved(node)) {
				out.writeUInt32(DEFINITION_ID_FIELD_NUMBER, node.definitionId, false);
				out.writeObject(NODE_FIELD_NUMBER, node, getSchema(node.getClass()), false);
        	}
        }
        EntityDefinition definition = entity.getDefinition();
        List<NodeDefinition> childDefinitions = definition.getChildDefinitions();
        for (NodeDefinition childDefinition : childDefinitions) {
        	State childState = entity.getChildState(childDefinition);
        	out.writeInt32(CHILD_NODE_STATE_FIELD_NUMBER, childState.intValue(), false);
        	out.writeInt32(CHILD_DEFINITION_ID_FIELD_NUMBER, childDefinition.getId(), false);
        }
	}

	@Override
	public void mergeFrom(Input input, Entity entity) throws IOException {
        for(int number = input.readFieldNumber(this); number > 0 ; number = input.readFieldNumber(this)) {
        	switch (number) {
        	case DEFINITION_ID_FIELD_NUMBER:
        		Schema idmSchema = entity.getSchema();
        		
        		// Definition id
        		int definitionId = input.readUInt32();
        		NodeDefinition defn = idmSchema.getDefinitionById(definitionId);
        		if ( defn == null || ( defn instanceof AttributeDefinition && ((AttributeDefinition) defn).isCalculated() ) ) {
	        		skipNode(input);
        		} else {
	        		Node<?> node = defn.createNode();
	        		entity.add(node);
	        		// Node
	        		readAndCheckFieldNumber(input, NODE_FIELD_NUMBER);
	        		input.mergeObject(node, getSchema(node.getClass()));
        		}
        		break;
        	case CHILD_NODE_STATE_FIELD_NUMBER:
        		//Node state
        		int intState = input.readInt32();
        		State state = State.parseState(intState);
        		readAndCheckFieldNumber(input, CHILD_DEFINITION_ID_FIELD_NUMBER);
        		int childDefnId = input.readInt32();
        		Schema schema = entity.getSchema();
        		NodeDefinition childDefn = schema.getDefinitionById(childDefnId);
        		if ( childDefn != null ) {
        			entity.setChildState(childDefn, state);
        		}
        		break;
        	default:
            	throw new ProtostuffException("Unexpected field number");
            }
        }
	}

	protected boolean isNodeToBeSaved(Node<?> node) {
		if ( node instanceof Attribute<?, ?> && ! (((AttributeDefinition) node.getDefinition()).isCalculated() ) ) {
			Entity parent = node.getParent();
    		int count = parent.getCount(node.getDefinition());
    		if ( count == 1 && ! ((Attribute<?, ?>) node).hasData() ) {
    			return false;
    		}
    	}
		return true;
	}

	protected void skipNode(Input input) throws IOException, ProtostuffException {
		readAndCheckFieldNumber(input, NODE_FIELD_NUMBER);
		input.handleUnknownField(NODE_FIELD_NUMBER, this);
	}

}
