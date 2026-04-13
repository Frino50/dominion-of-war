<script lang="ts" setup>
defineProps<{
    /** Clé du champ dans l'objet de données */
    field: string;
    /** Label affiché dans le header */
    header: string;
    /** Largeur CSS optionnelle (ex: '80px', '20%') */
    width?: string;
    /** Classes CSS pour le <th> */
    headerClass?: string;
    /** Classes CSS pour chaque <td> */
    bodyClass?: string;
}>();

defineSlots<{
    /** Slot pour personnaliser le rendu de la cellule */
    body(props: { value: any; row: any; index: number }): any;
}>();
</script>
