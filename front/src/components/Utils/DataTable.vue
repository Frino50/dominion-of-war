<template>
    <div class="table-wrapper">
        <table class="data-table">
            <thead>
                <tr>
                    <th
                        v-for="col in columnDefs"
                        :key="col.field"
                        :class="col.headerClass"
                        :style="col.width ? { width: col.width } : {}"
                    >
                        {{ col.header }}
                    </th>
                    <th v-if="hasActions" class="col-actions">Actions</th>
                </tr>
            </thead>
            <tbody>
                <tr
                    v-for="(row, rowIndex) in data"
                    :key="rowKey ? (row as any)[rowKey] : rowIndex"
                    :class="{ editing: rowEditing?.(row) }"
                >
                    <td
                        v-for="col in columnDefs"
                        :key="col.field"
                        :class="col.bodyClass"
                    >
                        <component
                            v-if="col.bodySlot"
                            :is="
                                () =>
                                    col.renderFns.get(col.field)!(row, rowIndex)
                            "
                        />
                        <template v-else>
                            {{ (row as any)[col.field] ?? "-" }}
                        </template>
                    </td>

                    <td v-if="hasActions" class="col-actions">
                        <div class="action-buttons">
                            <slot name="actions" :row="row" :index="rowIndex" />
                        </div>
                    </td>
                </tr>

                <tr v-if="data.length === 0">
                    <td
                        :colspan="columnDefs.length + (hasActions ? 1 : 0)"
                        class="empty-state"
                    >
                        <slot name="empty">{{ emptyText }}</slot>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</template>

<script lang="ts" setup generic="T">
import {
    computed,
    shallowRef,
    watchEffect,
    useSlots,
    type VNode,
    type Slot,
} from "vue";

withDefaults(
    defineProps<{
        data: T[];
        rowKey?: string;
        emptyText?: string;
        rowEditing?: (row: T) => boolean;
    }>(),
    {
        emptyText: "Aucune donnée.",
    }
);

interface ColumnDef {
    field: string;
    header: string;
    width?: string;
    headerClass?: string;
    bodyClass?: string;
    bodySlot?: Slot;
    renderFns: Map<string, (row: any, index: number) => any>;
}

const slots = useSlots();
const hasActions = computed(() => !!slots["actions"]);
const columnDefs = shallowRef<ColumnDef[]>([]);

watchEffect(() => {
    const defaultSlot = slots.default?.();
    if (!defaultSlot) {
        columnDefs.value = [];
        return;
    }

    columnDefs.value = defaultSlot
        .flatMap((vnode: VNode) => {
            if (Array.isArray(vnode.children)) {
                return vnode.children as VNode[];
            }
            return [vnode];
        })
        .filter((vnode: VNode) => vnode.props != null)
        .map((vnode: VNode) => {
            const props = vnode.props as Record<string, any>;
            const childSlots = vnode.children as Record<string, Slot> | null;
            const bodySlot = childSlots?.body;

            const renderFns = new Map<
                string,
                (row: any, index: number) => any
            >();
            if (bodySlot) {
                renderFns.set(props.field, (row: any, index: number) =>
                    bodySlot({ value: row[props.field], row, index })
                );
            }

            return {
                field: props.field,
                header: props.header,
                width: props.width,
                headerClass: props.headerClass ?? props["header-class"],
                bodyClass: props.bodyClass ?? props["body-class"],
                bodySlot,
                renderFns,
            } satisfies ColumnDef;
        });
});
</script>

<style scoped>
.table-wrapper {
    overflow-x: auto;
}

.data-table {
    width: 100%;
    border-collapse: collapse;
}

.data-table thead th {
    text-align: left;
    padding: 0.75rem 1rem;
    font-size: 0.75rem;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    color: var(--text-secondary);
    border-bottom: 1px solid var(--border-base);
    white-space: nowrap;
}

.data-table tbody td {
    padding: 0.875rem 1rem;
    border-bottom: 1px solid var(--border-base);
    color: var(--text-primary);
    vertical-align: middle;
}

.data-table tbody tr:last-child td {
    border-bottom: none;
}

.data-table tbody tr {
    transition: background-color var(--transition-base, 0.15s ease);
}

.data-table tbody tr:hover {
    background-color: var(--bg-hover);
}

.col-actions {
    width: 120px;
    text-align: right;
}

.action-buttons {
    display: flex;
    gap: 0.5rem;
    justify-content: flex-end;
}

.empty-state {
    text-align: center;
    padding: 3rem 1rem !important;
    color: var(--text-muted);
    font-style: italic;
}

:global(.data-table tbody tr.editing) {
    background-color: rgba(59, 130, 246, 0.1);
    box-shadow: inset 3px 0 0 var(--primary);
}
</style>
