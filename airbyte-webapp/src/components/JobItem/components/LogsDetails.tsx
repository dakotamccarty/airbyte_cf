import React from "react";
import styled from "styled-components";

import { AttemptRead, JobDebugInfoRead } from "../../../core/request/AirbyteClient";
import AttemptDetails from "./AttemptDetails";
import DebugInfoButton from "./DebugInfoButton";
import DownloadButton from "./DownloadButton";
import { LinkToAttemptButton } from "./LinkToAttemptButton";
import LogsTable from "./Logs";

const LogHeader = styled.div`
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 9px;
  font-size: 12px;
  padding: 0 10px;
`;

const AttemptDetailsSection = styled.div`
  padding-left: 10px;
  padding-top: 10px;
`;

const LogPath = styled.span`
  flex: 1;
  color: ${({ theme }) => theme.greyColor40};
`;

export const LogsDetails: React.FC<{
  id: number | string;
  path: string;
  currentAttempt?: AttemptRead;
  jobDebugInfo?: JobDebugInfoRead;
}> = ({ path, id, currentAttempt, jobDebugInfo }) => (
  <>
    {currentAttempt && (
      <AttemptDetailsSection>
        <AttemptDetails attempt={currentAttempt} />
      </AttemptDetailsSection>
    )}
    <LogHeader>
      <LogPath>{path}</LogPath>
      <LinkToAttemptButton jobId={id} attemptId={currentAttempt?.id} />
      {currentAttempt && <DownloadButton currentAttempt={currentAttempt} fileName={`logs-${id}`} />}
      {jobDebugInfo && <DebugInfoButton jobDebugInfo={jobDebugInfo} />}
    </LogHeader>
    <LogsTable logsArray={jobDebugInfo?.attempts.slice(-1)[0].logs.logLines} />
  </>
);
