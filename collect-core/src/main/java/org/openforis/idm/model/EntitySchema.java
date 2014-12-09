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

	private static final int FIELD_DEFINITION_ID = 1;
	private static final int FIELD_NODE = 2;
	private static final int FIELD_CHILD_NODE_STATE = 3;
	private static final int FIELD_CHILD_DEFINITION_ID = 4;

	public EntitySchema() {
		super(Entity.class, "children", "childStates");
	}

	@Override
	public String messageName() {
		return "entity";
	}

	@Override
	public void writeTo(Output out, Entity entity) throws IOException {
		List<Node<? extends NodeDefinition>> children = entity.getAll();
        for(Node<?> node : children) {
        	if(isNodeToBeSaved(node)) {
				out.writeUInt32(FIELD_DEFINITION_ID, node.definitionId, false);
				out.writeObject(FIELD_NODE, node, getSchema(node.getClass()), false);
        	}
        }
        EntityDefinition definition = entity.getDefinition();
        List<NodeDefinition> childDefinitions = definition.getChildDefinitions();
        for (NodeDefinition childDefinition : childDefinitions) {
        	String childName = childDefinition.getName();
        	State childState = entity.getChildState(childName);
        	out.writeInt32(FIELD_CHILD_NODE_STATE, childState.intValue(), false);
        	out.writeInt32(FIELD_CHILD_DEFINITION_ID, childDefinition.getId(), false);
        }
	}

	@Override
	public void mergeFrom(Input input, Entity entity) throws IOException {
        for(int number = input.readFieldNumber(this); ; number = input.readFieldNumber(this))
        {
        	if ( number == 0 ) {
        		break;
        	} else if ( number == FIELD_DEFINITION_ID ) {
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
	        		readAndCheckFieldNumber(input, FIELD_NODE);
	        		input.mergeObject(node, getSchema(node.getClass()));
        		}
        	} else if ( number == FIELD_CHILD_NODE_STATE ){
        		//Node state
        		int intState = input.readInt32();
        		State state = State.parseState(intState);
        		readAndCheckFieldNumber(input, FIELD_CHILD_DEFINITION_ID);
        		int childDefnId = input.readInt32();
        		Schema schema = entity.getSchema();
        		NodeDefinition childDefn = schema.getDefinitionById(childDefnId);
        		if ( childDefn != null ) {
        			entity.childStates.put(childDefn.getName(), state);
        		}
        	} else {
            	throw new ProtostuffException("Unexpected field number");
            }
        }
	}

	protected boolean isNodeToBeSaved(Node<?> node) {
		if ( node instanceof Attribute<?, ?> && ! (((AttributeDefinition) node.getDefinition()).isCalculated() ) ) {
			Entity parent = node.getParent();
    		int count = parent.getCount(node.getName());
    		if ( count == 1 && ! ((Attribute<?, ?>) node).hasData() ) {
    			return false;
    		}
    	}
		return true;
	}

	protected void skipNode(Input input) throws IOException, ProtostuffException {
		readAndCheckFieldNumber(input, FIELD_NODE);
		input.handleUnknownField(FIELD_NODE, this);
	}

}
