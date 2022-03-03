const CheckedIconFormatter = (props) => <span className={props.checked ? 'checked small' : ''}></span>

export const DataGridCellRenderers = {
  bool: ({ value }) => <CheckedIconFormatter checked={value} />,
}
