import { ROTULO_STATUS } from '../utils/formatadores';

function StatusBadge({ status }) {
  return <span className={`status status-${status.toLowerCase()}`}>{ROTULO_STATUS[status] ?? status}</span>;
}

export default StatusBadge;
