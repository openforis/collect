package org.jooq.impl;

import static org.jooq.Clause.CREATE_INDEX;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

import org.jooq.Clause;
import org.jooq.Configuration;
import org.jooq.Context;
import org.jooq.CreateIndexFinalStep;
import org.jooq.CreateIndexStep;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.CollectCreateIndexStep;
import org.jooq.QueryPart;
import org.jooq.Table;

/**
 * @author S. Ricci
 */
public class CollectCreateIndexImpl extends AbstractQuery implements

    // Cascading interface implementations for CREATE INDEX behaviour
    CollectCreateIndexStep,
    CreateIndexFinalStep {

    /**
     * Generated UID
     */
    private static final long     serialVersionUID = 8904572826501186329L;
    private static final Clause[] CLAUSES          = { CREATE_INDEX };

    private final Name            index;
    private Table<?>              table;
    private Field<?>[]            fields;
	private boolean               unique = false;

    public CollectCreateIndexImpl(Configuration configuration, Name index) {
        super(configuration);

        this.index = index;
    }

    // ------------------------------------------------------------------------
    // XXX: DSL API
    // ------------------------------------------------------------------------

    @Override
    public final CreateIndexFinalStep on(Table<?> t, Field<?>... f) {
        this.table = t;
        this.fields = f;

        return this;
    }

    @Override
    public final CreateIndexFinalStep on(String tableName, String... fieldNames) {
        Field<?>[] f = new Field[fieldNames.length];

        for (int i = 0; i < f.length; i++)
            f[i] = field(name(fieldNames[i]));

        return on(table(name(tableName)), f);
    }

    @Override
    public CreateIndexStep unique() {
    	this.unique = true;
    	return this;
    }
    
    // ------------------------------------------------------------------------
    // XXX: QueryPart API
    // ------------------------------------------------------------------------

    @Override
    public final void accept(Context<?> ctx) {
        ctx.keyword(unique ? "create unique index" : "create index")
	       .sql(' ')
	       .visit(index)
           .sql(' ')
           .keyword("on")
           .sql(' ')
           .visit(table)
           .sql('(')
           .qualify(false)
           .visit(new QueryPartList<QueryPart>(fields))
           .qualify(true)
           .sql(')');
    }

    @Override
    public final Clause[] clauses(Context<?> ctx) {
        return CLAUSES;
    }
}
